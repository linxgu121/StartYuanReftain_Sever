package cn.niuma.lingdi000721.startyuanreftain.config.security;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Objects;

/**
 * JWT签发的配置中心
 * 负责从配置文件中读取 Token 相关的安全参数，并在应用启动时校验合法性
 * 启动器与游戏使用不同受众，避免凭据混用
 */
@ConfigurationProperties(prefix = "niuma.security.jwt")
public final class JwtProperties {
    //签发者
    private final String issuer;
    //受众(启动器与游戏)
    private final String launcherAudience;
    private final String gameAudience;
    //Token 存活时长
    private final Duration accessTokenTtl;
    //编码后的密钥
    private final String secretBase64;

    public JwtProperties(
            String issuer,
            String launcherAudience,
            String gameAudience,
            Duration accessTokenTtl,
            String secretBase64) {

        this.issuer = requireText(issuer, "JWT issuer 不能为空");

        this.launcherAudience = requireText(launcherAudience, "启动器 audience 不能为空");

        this.gameAudience = requireText(gameAudience, "游戏 audience 不能为空");

        if (this.launcherAudience.equals(this.gameAudience))
        {
            throw new IllegalArgumentException("启动器与游戏不能使用相同 audience");
        }

        this.secretBase64 = requireText(secretBase64, "JWT Base64 密钥不能为空");

        this.accessTokenTtl = Objects.requireNonNull(accessTokenTtl, "JWT Access Token 有效期不能为空");

        if (accessTokenTtl.isZero() || accessTokenTtl.isNegative())
        {
            throw new IllegalArgumentException("JWT Access Token 有效期必须大于 0");
        }
    }

    public String getIssuer() {
        return issuer;
    }

    public String getLauncherAudience() {return launcherAudience;}

    public String getGameAudience() {return gameAudience;}

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
