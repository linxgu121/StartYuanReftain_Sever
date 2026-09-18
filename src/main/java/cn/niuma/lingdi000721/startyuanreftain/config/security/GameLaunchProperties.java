package cn.niuma.lingdi000721.startyuanreftain.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Set;

/**
 * 游戏启动与票据配置。
 *
 * 由服务端配置决定，不接受客户端自行指定有效期或运行环境。
 */
@ConfigurationProperties(prefix = "niuma.security.game-launch")
public record GameLaunchProperties(
        Duration ticketTtl,
        Duration gameAccessTokenTtl,
        String environment,
        Set<String> allowedGameIds)
{
    public GameLaunchProperties
    {
        // 第一版允许 10–120 秒，并要求整秒。
        if (ticketTtl == null
                || ticketTtl.getNano() != 0
                || ticketTtl.compareTo(Duration.ofSeconds(10)) < 0
                || ticketTtl.compareTo(Duration.ofSeconds(120)) > 0)
        {
            throw new IllegalArgumentException("票据有效期必须为 10–120 秒之间的整秒时长");
        }

        // 游戏令牌使用独立有效期，不等于自动续期。
        if (gameAccessTokenTtl == null
                || gameAccessTokenTtl.getNano() != 0
                || gameAccessTokenTtl.compareTo(Duration.ofMinutes(1)) < 0
                || gameAccessTokenTtl.compareTo(Duration.ofMinutes(30)) > 0)
        {
            throw new IllegalArgumentException("游戏令牌有效期必须为 1–30 分钟之间的整秒时长");
        }

        if (environment == null || !environment.matches("[a-z0-9][a-z0-9-]{0,31}"))
        {
            throw new IllegalArgumentException("运行环境标识格式无效");
        }

        if (allowedGameIds == null || allowedGameIds.isEmpty())
        {
            throw new IllegalArgumentException("允许的游戏列表不能为空");
        }

        for (String gameId : allowedGameIds)
        {
            if (gameId == null || !gameId.matches("[a-z0-9][a-z0-9._-]{0,63}"))
            {
                throw new IllegalArgumentException("允许的游戏列表包含无效标识");
            }
        }

        // 防止业务代码拿到集合后修改服务端允许列表。
        allowedGameIds = Set.copyOf(allowedGameIds);
    }
}
