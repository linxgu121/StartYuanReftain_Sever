package cn.niuma.lingdi000721.startyuanreftain.config.security;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

/**
 * JWT签发的配置中心
 * 负责从配置文件中读取 Token 相关的安全参数，并在应用启动时校验合法性
 */
@ConfigurationProperties(prefix = "niuma.security.jwt")
public final class JwtProperties {
    //签发者
    private final String issuer;
    //受众
    private final String audience;
    //Token 存活时长
    private final Duration accessTokenTtl;
    //编码后的密钥
    private final String secretBase64;

    public JwtProperties(
            String issuer,
            String audience,
            Duration accessTokenTtl,
            String secretBase64) {

        this.issuer = requireText(issuer, "JWT issuer 不能为空");
        this.audience = requireText(audience, "JWT audience 不能为空");
        this.secretBase64 = requireText(secretBase64, "JWT Base64 密钥不能为空");

        this.accessTokenTtl = Objects.requireNonNull(accessTokenTtl, "JWT Access Token 有效期不能为空");

        if (accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            throw new IllegalArgumentException("JWT Access Token 有效期必须大于 0");
        }
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAudience() {
        return audience;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public String getSecretBase64() {
        return secretBase64;
    }

    private static String requireText(
            String value,
            String message) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

}
