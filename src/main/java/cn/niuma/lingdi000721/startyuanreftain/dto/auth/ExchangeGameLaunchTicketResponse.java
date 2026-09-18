package cn.niuma.lingdi000721.startyuanreftain.dto.auth;

/**
 * 游戏获得自己的访问令牌。
 * 这里不是启动器令牌，也不包含刷新令牌。
 */
public record ExchangeGameLaunchTicketResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String playerUid,
        String gameId)
{
    public ExchangeGameLaunchTicketResponse
    {
        if (accessToken == null || accessToken.isBlank())
        {
            throw new IllegalArgumentException("游戏令牌不能为空");
        }

        if (!"Bearer".equals(tokenType))
        {
            throw new IllegalArgumentException("令牌类型必须为 Bearer");
        }

        if (expiresInSeconds <= 0)
        {
            throw new IllegalArgumentException("游戏令牌有效期必须大于 0");
        }

        if (playerUid == null
                || !playerUid.matches("[1-9][0-9]{8}"))
        {
            throw new IllegalArgumentException("玩家 UID 格式无效");
        }

        if (gameId == null
                || !gameId.matches("[a-z0-9][a-z0-9._-]{0,63}"))
        {
            throw new IllegalArgumentException("游戏标识格式无效");
        }
    }

    @Override
    public String toString()
    {
        return "ExchangeGameLaunchTicketResponse[accessToken=<redacted>]";
    }
}
