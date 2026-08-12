package cn.niuma.lingdi000721.startyuanreftain.common.dto.account;

import java.util.Objects;
import java.util.UUID;

/**
 * 注册响应
 */
public record RegisterAccountResponse(
        UUID accountUuid,
        UUID warehouseUuid)
{
    public RegisterAccountResponse
    {
        Objects.requireNonNull(accountUuid,"accountUuid 不能为空");
        Objects.requireNonNull(warehouseUuid,"warehouseUuid 不能为空");

    }
}
