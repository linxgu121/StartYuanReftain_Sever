package cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 完整替换当前账号快捷栏持久化快照的内部业务命令。
 *
 * 与 HTTP DTO 分离，避免事务服务依赖 Web 层。
 */
public record SaveQuickSlotSnapshotCommand(
        UUID accountUuid,
        int schemaVersion,
        long expectedRevision,
        List<QuickSlotBindingCommand> bindings)
{
    private static final int SUPPORTED_SCHEMA_VERSION = 1;

    private static final UUID EMPTY_ACCOUNT_UUID = new UUID(0L, 0L);

    public SaveQuickSlotSnapshotCommand
    {
        Objects.requireNonNull(
                accountUuid,
                "accountUuid 不能为空");

        Objects.requireNonNull(
                bindings,
                "bindings 不能为空");

        if (EMPTY_ACCOUNT_UUID.equals(accountUuid))
        {
            throw new IllegalArgumentException("accountUuid 不能是全零 UUID");
        }

        if (schemaVersion != SUPPORTED_SCHEMA_VERSION)
        {
            throw new IllegalArgumentException("当前服务端仅支持快捷栏协议版本 1");
        }

        if (expectedRevision < 0L)
        {
            throw new IllegalArgumentException("expectedRevision 不能小于 0");
        }

        if (bindings.size() > 256)
        {
            throw new IllegalArgumentException("快捷栏绑定数量不能超过 256");
        }

        List<QuickSlotBindingCommand> copiedBindings =
                new ArrayList<>(bindings.size());

        Set<Integer> usedSlots = new HashSet<>();

        for (QuickSlotBindingCommand binding : bindings)
        {
            Objects.requireNonNull(
                    binding,
                    "bindings 不能包含 null");

            if (!usedSlots.add(binding.slotIndex()))
            {
                throw new IllegalArgumentException(
                        "bindings 包含重复槽位："
                                + binding.slotIndex());
            }

            copiedBindings.add(binding);
        }

        copiedBindings.sort(
                Comparator.comparingInt(
                        QuickSlotBindingCommand::slotIndex));

        bindings = List.copyOf(copiedBindings);
    }
}