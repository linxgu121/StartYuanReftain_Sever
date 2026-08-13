package cn.niuma.lingdi000721.startyuanreftain.service.security;

import java.time.Instant;
import java.util.Objects;

/**
 * JWT 签发后的"凭证包裹"
 * 把 Token 字符串和它的生命周期元数据打包成一个不可变的值对象
 * 从 Service 层传递给 Controller，最终序列化后发给 UE 客户端
 */
public record IssuedAccessToken(
        String tokenValue,
        Instant issuedAt,
        Instant expiresAt)
{

    public IssuedAccessToken {
        Objects.requireNonNull(tokenValue, "Token 内容不能为空");

        Objects.requireNonNull(issuedAt, "签发时间不能为空");

        Objects.requireNonNull(expiresAt, "过期时间不能为空");

        if (tokenValue.isBlank()) {
            throw new IllegalArgumentException("Token 内容不能为空");
        }

        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("Token 过期时间必须晚于签发时间");
        }
    }

    @Override
    public String toString() {
        return "IssuedAccessToken["
                + "tokenValue=<redacted>, "
                + "issuedAt=" + issuedAt
                + ", expiresAt=" + expiresAt
                + ']';
    }
}
