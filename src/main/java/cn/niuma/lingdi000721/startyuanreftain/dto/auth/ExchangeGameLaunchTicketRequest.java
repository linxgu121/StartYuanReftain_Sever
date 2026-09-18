package cn.niuma.lingdi000721.startyuanreftain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * 游戏客户端使用票据兑换游戏令牌
 * 不需要再次提交用户名和密码。
 */
public record ExchangeGameLaunchTicketRequest(

        @NotBlank(message = "启动票据不能为空")
        @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "启动票据格式无效")
        String ticket,

        @NotBlank(message = "游戏标识不能为空")
        @Pattern(regexp = "[a-z0-9][a-z0-9._-]{0,63}", message = "游戏标识格式无效")
        String gameId,

        @NotNull(message = "启动请求编号不能为空")
        UUID launchRequestId)
{
    @Override
    public String toString()
    {
        return "ExchangeGameLaunchTicketRequest[ticket=<redacted>]";
    }
}
