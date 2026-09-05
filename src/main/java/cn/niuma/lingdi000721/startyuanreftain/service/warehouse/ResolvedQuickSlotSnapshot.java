package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 已通过结构与持久化所有权校验的快捷栏只读快照。
 *
 * persistenceId 只供服务端继续查询和更新，
 * 不会发送给 Unity。
 */
public record ResolvedQuickSlotSnapshot(
        long persistenceId,
        long revision,
        int schemaVersion,
        List<ResolvedQuickSlotBinding> bindings)
{
    public ResolvedQuickSlotSnapshot
    {
        if (persistenceId <= 0L)
        {
            throw new IllegalArgumentException(
                    "persistenceId 必须大于 0");
        }

        if (revision < 0L)
        {
            throw new IllegalArgumentException(
                    "revision 不能小于 0");
        }

        if (schemaVersion < 1)
        {
            throw new IllegalArgumentException("schemaVersion 必须大于等于 1");
        }

        Objects.requireNonNull(
                bindings, "bindings 不能为空");

        List<ResolvedQuickSlotBinding> copiedBindings =
                new ArrayList<>(bindings.size());

        Set<Integer> usedSlots = new HashSet<>();

        for (ResolvedQuickSlotBinding binding : bindings)
        {
            if (binding == null)
            {
                throw new IllegalArgumentException("bindings 不能包含 null");
            }

            if (!usedSlots.add(binding.slotIndex()))
            {
                throw new IllegalArgumentException("bindings 包含重复槽位：" + binding.slotIndex());
            }

            copiedBindings.add(binding);
        }

        copiedBindings.sort(
                Comparator.comparingInt(
                        ResolvedQuickSlotBinding::slotIndex));

        bindings = List.copyOf(copiedBindings);
    }
}