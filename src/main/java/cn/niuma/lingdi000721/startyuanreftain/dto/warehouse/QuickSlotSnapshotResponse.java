package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import java.util.List;
import java.util.Objects;

/**
 * 返回给 Unity 的完整快捷栏持久化快照。
 *
 * 不包含容量、当前选中槽位或游戏会话 Revision。
 */
public record QuickSlotSnapshotResponse(
        int schemaVersion,
        long revision,
        List<QuickSlotBindingResponse> bindings)
{
    public QuickSlotSnapshotResponse
    {
        if (schemaVersion < 1)
        {
            throw new IllegalArgumentException(
                    "schemaVersion 必须大于等于 1");
        }

        if (revision < 0L)
        {
            throw new IllegalArgumentException(
                    "revision 不能小于 0");
        }

        Objects.requireNonNull(
                bindings,
                "bindings 不能为空");

        bindings = List.copyOf(bindings);
    }
}