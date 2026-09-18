package cn.niuma.lingdi000721.startyuanreftain.dto.auth;

import java.util.Objects;
import java.util.UUID;

/**
 * 申请成功后返回的一次性票据。
 * 外层仍由 ApiResponse 包装。
 */
public record CreateGameLaunchTicketResponse(
        String ticket,
        String gameId,
        UUID launchRequestId,
        long expiresInSeconds)
{
    public CreateGameLaunchTicketResponse
    {
        if (ticket == null || !ticket.matches("[A-Za-z0-9_-]{43}"))
        {
            throw new IllegalArgumentException("启动票据格式无效");
        }

        if (gameId == null || !gameId.matches("[a-z0-9][a-z0-9._-]{0,63}"))
        {
            throw new IllegalArgumentException("游戏标识格式无效");
        }

        Objects.requireNonNull(launchRequestId, "启动请求编号不能为空");

        if (expiresInSeconds <= 0)
        {
            throw new IllegalArgumentException("票据有效期必须大于 0");
        }
    }

    /**
     * record 默认会打印全部字段，必须隐藏票据。
     */
    @Override
    public String toString()
    {
        return "CreateGameLaunchTicketResponse[ticket=<redacted>]";
    }
}
