package cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command;

import java.util.Objects;
import java.util.UUID;

/**
 * Web 请求转换后的单条快捷栏绑定命令。
 *
 * 此时只完成协议结构校验，尚未证明非空物品属于当前玩家。
 */
public record QuickSlotBindingCommand(
        int slotIndex,
        UUID itemInstanceId)
{
    private static final UUID EMPTY_INSTANCE_ID =
            new UUID(0L, 0L);

    public QuickSlotBindingCommand
    {
        Objects.requireNonNull(
                itemInstanceId,
                "itemInstanceId 不能为空");

        if (slotIndex < 0 || slotIndex > 255)
        {
            throw new IllegalArgumentException(
                    "slotIndex 必须处于 0 到 255");
        }
    }

    /**
     * false 表示显式空槽，持久化时不会生成数据库记录。
     */
    public boolean hasItem()
    {
        return !EMPTY_INSTANCE_ID.equals(itemInstanceId);
    }
}