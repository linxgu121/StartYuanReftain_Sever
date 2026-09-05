package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import java.util.Objects;
import java.util.UUID;

/**
 * 已通过持久化结构校验的一条非空快捷栏绑定。
 */
public record ResolvedQuickSlotBinding(
        int slotIndex,
        UUID itemInstanceId)
{
    private static final int MAX_SLOT_INDEX = 255;
    private static final UUID EMPTY_INSTANCE_ID =
            new UUID(0L, 0L);

    public ResolvedQuickSlotBinding
    {
        Objects.requireNonNull(itemInstanceId, "itemInstanceId 不能为空");

        if (slotIndex < 0 || slotIndex > MAX_SLOT_INDEX)
        {
            throw new IllegalArgumentException("slotIndex 必须处于 0 到 255");
        }

        if (EMPTY_INSTANCE_ID.equals(itemInstanceId))
        {
            throw new IllegalArgumentException("持久化非空绑定不能使用全零 UUID");
        }
    }
}