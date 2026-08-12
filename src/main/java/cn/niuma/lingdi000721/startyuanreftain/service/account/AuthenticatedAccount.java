package cn.niuma.lingdi000721.startyuanreftain.service.account;

import java.util.Objects;
import java.util.UUID;

/**
 * 已通过凭据与状态检查的内部账号身份。
 * 这不是 HTTP 响应 DTO，不包含密码哈希和数据库主键。
 */
public record AuthenticatedAccount(
        UUID accountUuid,
        String playerUid)
{
    public AuthenticatedAccount
    {
        Objects.requireNonNull(accountUuid,"accountUuid 不能为空");

        Objects.requireNonNull(playerUid, "playerUid 不能为空");

        if (!playerUid.matches("[1-9]\\d{8}"))
        {
            throw new IllegalArgumentException("playerUid 必须是九位十进制数字");
        }
    }
}
