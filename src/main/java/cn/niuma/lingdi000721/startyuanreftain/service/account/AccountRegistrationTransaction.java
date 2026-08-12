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

    private final PlayerUidGenerator playerUidGenerator;
    private final AccountMapper accountMapper;
    private final PlayerWarehouseMapper warehouseMapper;

    public AccountRegistrationTransaction(
            AccountMapper accountMapper,
            PlayerWarehouseMapper warehouseMapper,
            PlayerUidGenerator playerUidGenerator)
    {
        this.accountMapper = Objects.requireNonNull(accountMapper, "accountMapper 不能为空");

        this.warehouseMapper = Objects.requireNonNull(warehouseMapper, "warehouseMapper 不能为空");

        this.playerUidGenerator = Objects.requireNonNull(playerUidGenerator,"playerUidGenerator 不能为空");
    }

    //方法内所有数据库操作，要么全部成功提交，要么全部回滚，不会出现半成功状态
    @Transactional
    public RegisterAccountResponse createAccountAndWarehouse(
            String username,
            String passwordHash)
    {
        Objects.requireNonNull(username, "username 不能为空");
        Objects.requireNonNull(passwordHash, "passwordHash 不能为空");

        UUID accountUuid = UUID.randomUUID();
        long playerUid = playerUidGenerator.generate();

        Account account = new Account(
                accountUuid.toString(),
                playerUid,
                username,
                passwordHash);

        insertAccount(account);

        Long accountId = account.getId();

        if (accountId == null || accountId <= 0)
        {
            throw new IllegalStateException("账号插入成功但数据库没有回填有效主键");
        }

        UUID warehouseUuid = UUID.randomUUID();

        PlayerWarehouse warehouse = new PlayerWarehouse(warehouseUuid.toString(), accountId);

        int insertedWarehouses = warehouseMapper.insert(warehouse);

        if (insertedWarehouses != 1)
        {
            throw new IllegalStateException("初始仓库插入影响行数不是 1");
        }

        return new RegisterAccountResponse(accountUuid,Long.toString(playerUid), warehouseUuid);
    }

    private void insertAccount(Account account)
    {
        try
        {
            int insertedAccounts =  accountMapper.insert(account);

            if (insertedAccounts != 1)
            {
                throw new IllegalStateException("账号插入影响行数不是 1");
            }
        }
        catch (DuplicateKeyException exception)
        {
            if (isUsernameConstraintViolation(exception))
            {
                throw new BusinessException(
                        AccountErrorCode.USERNAME_ALREADY_EXISTS);
            }

            /*
             * account_uuid 等其他唯一约束冲突不能伪装成用户名重复。
             * 交给全局异常处理器记录并返回内部错误。
             */
            throw exception;
        }
    }

    /**
     * 判断唯一键冲突是否来自用户名唯一索引 uk_account_username
     */
    private boolean isUsernameConstraintViolation(DuplicateKeyException exception)
    {
        // 获取整条异常链最底层的原始异常
        Throwable cause = exception.getMostSpecificCause();
        // 获取数据库原生报错文本
        String message = cause.getMessage();

        // 非空 + 包含索引名 → 判定为用户名重复冲突
        return message != null && message.contains(USERNAME_UNIQUE_CONSTRAINT);
    }
}
