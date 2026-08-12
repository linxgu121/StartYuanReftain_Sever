package cn.niuma.lingdi000721.startyuanreftain.dto.account;

import java.util.Objects;
import java.util.UUID;

/**
 * 注册响应
 * playerUid 是展示给玩家的公开编号，
 * accountUuid 是服务端认证使用的稳定身份
 */
public record RegisterAccountResponse(
        UUID accountUuid,
        String playerUid,
        UUID warehouseUuid)
{
    public RegisterAccountResponse
    {
        Objects.requireNonNull(accountUuid,"accountUuid 不能为空");
        Objects.requireNonNull(playerUid, "playerUid 不能为空");
        Objects.requireNonNull(warehouseUuid,"warehouseUuid 不能为空");

        if (!playerUid.matches("[1-9]\\d{8}"))
        {
            throw new IllegalArgumentException("playerUid 必须是九位十进制数字");
        }
    }
}
