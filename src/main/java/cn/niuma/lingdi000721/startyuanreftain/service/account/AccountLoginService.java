package cn.niuma.lingdi000721.startyuanreftain.service.account;

import cn.niuma.lingdi000721.startyuanreftain.dto.account.LoginAccountRequest;
import cn.niuma.lingdi000721.startyuanreftain.dto.account.LoginAccountResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.security.AccessTokenService;
import cn.niuma.lingdi000721.startyuanreftain.service.security.IssuedAccessToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * 账号登录的业务编排层（Facade），把"验证身份"和"签发凭证"两个子系统串起来
 * 输出 UE 客户端登录后需要的完整响应
 */
@Service
public final class AccountLoginService {
    private final AccountAuthenticationService authenticationService;
    private final AccessTokenService accessTokenService;

    public AccountLoginService(
            AccountAuthenticationService authenticationService,
            AccessTokenService accessTokenService)
    {
        this.authenticationService = Objects.requireNonNull(
                authenticationService,
                "authenticationService 不能为空");

        this.accessTokenService = Objects.requireNonNull(
                accessTokenService,
                "accessTokenService 不能为空");
    }

    public LoginAccountResponse login(
            LoginAccountRequest request)
    {
        Objects.requireNonNull(
                request,
                "request 不能为空");

        AuthenticatedAccount account =
                authenticationService.authenticate(request);

        IssuedAccessToken token =
                accessTokenService.issue(account);

        long expiresInSeconds = Duration.between(
                        token.issuedAt(),
                        token.expiresAt())
                .toSeconds();

        return new LoginAccountResponse(
                token.tokenValue(),
                LoginAccountResponse.BEARER_TOKEN_TYPE,
                expiresInSeconds,
                account.playerUid());
    }
}
