package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 快捷栏保存请求中的单个槽位。
 *
 * 全零 UUID 表示显式清空该槽位。Service 在持久化前会将其过滤，
 * 数据库只保存真正绑定了物品的槽位。
 */
public record QuickSlotBindingRequest(
        @NotNull(message = "快捷栏槽位下标不能为空")
        @Min(value = 0, message = "快捷栏槽位下标不能小于 0")
        @Max(value = 255, message = "快捷栏槽位下标不能大于 255")
        Integer slotIndex,

        @NotNull(message = "快捷栏物品实例 ID 不能为空")
        UUID itemInstanceId)
{
}