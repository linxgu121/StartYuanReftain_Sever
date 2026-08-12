package cn.niuma.lingdi000721.startyuanreftain.service.account;

import cn.niuma.lingdi000721.startyuanreftain.dto.account.RegisterAccountResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.entity.Account;
import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerWarehouse;
import cn.niuma.lingdi000721.startyuanreftain.enums.AccountErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.mapper.AccountMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerWarehouseMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * 账号注册的核心事务服务
 */
@Service
public class AccountRegistrationTransaction {
    //数据库层面保证 `username` 不能重复，防止两个人注册同名账号
    private static final String USERNAME_UNIQUE_CONSTRAINT = "uk_account_username";
    private static final String PLAYER_UID_UNIQUE_CONSTRAINT = "uk_account_player_uid";

    private static final int MAX_PLAYER_UID_ATTEMPTS = 8;

    private final PlayerUidGenerator playerUidGenerator;
    private final AccountMapper accountMapper;
    private final PlayerWarehouseMapper warehouseMapper;

    public AccountRegistrationTransaction(
            AccountMapper accountMapper,
            PlayerWarehouseMapper warehouseMapper,
            PlayerUidGenerator playerUidGenerator) {
        this.accountMapper = Objects.requireNonNull(accountMapper, "accountMapper 不能为空");

        this.warehouseMapper = Objects.requireNonNull(warehouseMapper, "warehouseMapper 不能为空");

        this.playerUidGenerator = Objects.requireNonNull(playerUidGenerator, "playerUidGenerator 不能为空");
    }

    //方法内所有数据库操作，要么全部成功提交，要么全部回滚，不会出现半成功状态
    @Transactional
    public RegisterAccountResponse createAccountAndWarehouse(
            String username,
            String passwordHash) {
        Objects.requireNonNull(username, "username 不能为空");
        Objects.requireNonNull(passwordHash, "passwordHash 不能为空");

        UUID accountUuid = UUID.randomUUID();

        Account account = insertAccountWithUniquePlayerUid(
                accountUuid,
                username,
                passwordHash);

        Long accountId = account.getId();

        if (accountId == null || accountId <= 0) {
            throw new IllegalStateException("账号插入成功但数据库没有回填有效主键");
        }

        UUID warehouseUuid = UUID.randomUUID();

        PlayerWarehouse warehouse = new PlayerWarehouse(warehouseUuid.toString(), accountId);

        int insertedWarehouses = warehouseMapper.insert(warehouse);

        if (insertedWarehouses != 1) {
            throw new IllegalStateException("初始仓库插入影响行数不是 1");
        }

        return new RegisterAccountResponse(accountUuid, Long.toString(account.getPlayerUid()), warehouseUuid);
    }

    /**
     * 生成候选 UID 并尝试插入账号。
     * 不使用“先查询再插入”，因为并发请求可能同时查询到 UID 不存在。
     * 数据库唯一约束才是最终裁决者。
     */
    private Account insertAccountWithUniquePlayerUid(
            UUID accountUuid,
            String username,
            String passwordHash) {
        for (int attempt = 1; attempt <= MAX_PLAYER_UID_ATTEMPTS; attempt++) {
            long playerUid = playerUidGenerator.generate();

            // 每次重试都创建新实体，避免复用失败插入后的实体状态。
            Account account = new Account(
                    accountUuid.toString(),
                    playerUid,
                    username,
                    passwordHash);

            try {
                int insertedAccounts = accountMapper.insert(account);

                if (insertedAccounts != 1) {
                    throw new IllegalStateException("账号插入影响行数不是 1");
                }

                return account;
            } catch (DuplicateKeyException exception) {
                if (isConstraintViolation(
                        exception,
                        USERNAME_UNIQUE_CONSTRAINT)) {
                    throw new BusinessException(AccountErrorCode.USERNAME_ALREADY_EXISTS);
                }

                if (!isConstraintViolation(
                        exception,
                        PLAYER_UID_UNIQUE_CONSTRAINT)) {
                    // account_uuid 等其他冲突或数据库异常不能被隐藏。
                    throw exception;
                }

                if (attempt == MAX_PLAYER_UID_ATTEMPTS) {
                    throw new IllegalStateException("无法在限定次数内分配唯一玩家 UID", exception);
                }
            }
        }

        // 理论上循环内必定成功返回或抛出异常。
        throw new IllegalStateException("玩家 UID 分配流程异常结束");
    }

    private boolean isConstraintViolation(
            DuplicateKeyException exception,
            String constraintName) {
        Throwable cause = exception.getMostSpecificCause();

        String message = cause.getMessage();

        return message != null && message.contains(constraintName);
    }
}