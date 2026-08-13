package cn.niuma.lingdi000721.startyuanreftain.dto.account;

import java.util.Objects;

/**
 *  JWT 签发后的"凭证包裹"，把 Token 字符串和它的生命周期打包成一个不可变的值对象
 *  从登录 Service 传递到 Controller
 */
public record LoginAccountResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String playerUid)
{
    public static final String BEARER_TOKEN_TYPE = "Bearer";

    public LoginAccountResponse
    {
        Objects.requireNonNull(
                accessToken, "accessToken 不能为空");

        Objects.requireNonNull(
                tokenType, "tokenType 不能为空");

        Objects.requireNonNull(
                playerUid, "playerUid 不能为空");

        if (accessToken.isBlank())
        {
            throw new IllegalArgumentException("accessToken 不能为空");
        }

        if (!BEARER_TOKEN_TYPE.equals(tokenType))
        {
            throw new IllegalArgumentException("tokenType 必须为 Bearer");
        }

        if (expiresInSeconds <= 0)
        {
            throw new IllegalArgumentException("expiresInSeconds 必须大于 0");
        }

        if (!playerUid.matches("[1-9]\\d{8}"))
        {
            throw new IllegalArgumentException("playerUid 必须是九位十进制数字");
        }

    }

    @Override
    public String toString()
    {
        return "LoginAccountResponse["
                + "accessToken=<redacted>, "
                + "tokenType=" + tokenType
                + ", expiresInSeconds=" + expiresInSeconds
                + ", playerUid=" + playerUid
                + ']';
    }
}

