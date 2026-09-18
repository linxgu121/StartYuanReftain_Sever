package cn.niuma.lingdi000721.startyuanreftain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * 启动器申请游戏启动的票据
 *
 * 账号的token认证从启动器中获得
 */
public record CreateGameLaunchTicketRequest(

        @NotBlank(message = "游戏标识不能为空")
        @Pattern(regexp = "[a-z0-9][a-z0-9._-]{0,63}", message = "游戏标识格式无效")
        String gameId,

        @NotNull(message = "启动请求编号不能为空")
        UUID launchRequestId)
{

}
