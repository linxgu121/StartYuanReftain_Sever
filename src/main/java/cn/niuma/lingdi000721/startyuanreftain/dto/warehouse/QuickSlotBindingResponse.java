package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import java.util.Objects;
import java.util.UUID;

/**
 * 返回给 Unity 的一条非空快捷栏绑定。
 */
public record QuickSlotBindingResponse(
        int slotIndex,
        UUID itemInstanceId)
{
    private static final UUID EMPTY_INSTANCE_ID =
            new UUID(0L, 0L);

    public QuickSlotBindingResponse
    {
        Objects.requireNonNull(
                itemInstanceId,
                "itemInstanceId 不能为空");

        if (slotIndex < 0 || slotIndex > 255)
        {
            throw new IllegalArgumentException(
                    "slotIndex 必须处于 0 到 255");
        }

        if (EMPTY_INSTANCE_ID.equals(itemInstanceId))
        {
            throw new IllegalArgumentException(
                    "快捷栏响应不能包含全零物品 UUID");
        }
    }
}