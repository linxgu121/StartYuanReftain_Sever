package cn.niuma.lingdi000721.startyuanreftain.service.account;


import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.dto.account.LoginAccountRequest;
import cn.niuma.lingdi000721.startyuanreftain.entity.Account;
import cn.niuma.lingdi000721.startyuanreftain.enums.AccountErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.enums.AccountStatus;
import cn.niuma.lingdi000721.startyuanreftain.mapper.AccountMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * 验证用户名、密码和账号状态。
 *
 * 本服务只证明账号身份，不负责签发 JWT。
 */
@Service
public final class AccountAuthenticationService {

    //缩小账号不存在与密码错误之间的响应耗时差异
    private static final String DUMMY_PASSWORD =  "Niuma-Dummy-Credential-Only!";

    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户不存在时使用的虚拟 BCrypt 哈希。
     * 只在服务创建时计算一次，不在每次登录时重新编码。
     */
    private final String dummyPasswordHash;

    public AccountAuthenticationService(
            AccountMapper accountMapper,
            PasswordEncoder passwordEncoder)
    {
        this.accountMapper = Objects.requireNonNull(accountMapper, "accountMapper 不能为空");

        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder 不能为空");

        this.dummyPasswordHash = passwordEncoder.encode(DUMMY_PASSWORD);
    }

    public AuthenticatedAccount authenticate(LoginAccountRequest request)
    {
        Objects.requireNonNull(request, "request 不能为空");

        Account account = accountMapper.selectByUsername(request.username());

        if (account == null)
        {
            /*
             * 即使用户名不存在也执行 BCrypt，
             * 缩小与“账号存在但密码错误”之间的耗时差异。
             */
            passwordEncoder.matches(request.password(), dummyPasswordHash);

            throw invalidCredentials();
        }

        boolean passwordMatches = passwordEncoder.matches(request.password(), account.getPasswordHash());

        if (!passwordMatches)
        {
            throw invalidCredentials();
        }

        /*
         * 只有密码正确后才暴露封禁状态。
         * 否则攻击者可以通过错误类型判断账号存在。
         */
        if (account.getStatus() == AccountStatus.BANNED)
        {
            throw new BusinessException(AccountErrorCode.ACCOUNT_BANNED);
        }

        if (account.getStatus() != AccountStatus.ACTIVE)
        {
            throw new IllegalStateException("账号存在未知状态");
        }

        return new AuthenticatedAccount(
                UUID.fromString(account.getAccountUuid()),
                Long.toString(account.getPlayerUid()));
    }

    private BusinessException invalidCredentials()
    {
        return new BusinessException(AccountErrorCode.INVALID_CREDENTIALS);
    }


}
