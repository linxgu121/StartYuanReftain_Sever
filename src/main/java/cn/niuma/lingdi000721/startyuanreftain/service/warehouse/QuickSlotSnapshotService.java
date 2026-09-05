package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerQuickSlot;
import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerQuickSlotBinding;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerQuickSlotBindingMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerQuickSlotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 在同一个只读事务中加载并验证玩家快捷栏持久化快照。
 */
@Service
public class QuickSlotSnapshotService {
    private final PlayerQuickSlotMapper quickSlotMapper;
    private final PlayerQuickSlotBindingMapper bindingMapper;

    public QuickSlotSnapshotService(
            PlayerQuickSlotMapper quickSlotMapper,
            PlayerQuickSlotBindingMapper bindingMapper)
    {
        this.quickSlotMapper = Objects.requireNonNull(
                quickSlotMapper,
                "quickSlotMapper 不能为空");

        this.bindingMapper = Objects.requireNonNull(
                bindingMapper,
                "bindingMapper 不能为空");
    }

    /**
     * 加载指定账号必须拥有的完整快捷栏快照。
     */
    @Transactional(readOnly = true)
    public ResolvedQuickSlotSnapshot loadRequiredByAccountUuid(
            UUID accountUuid)
    {
        Objects.requireNonNull(
                accountUuid,
                "accountUuid 不能为空");

        PlayerQuickSlot quickSlot =
                quickSlotMapper.selectByAccountUuid(
                        accountUuid.toString());

        if (quickSlot == null)
        {
            throw new IllegalStateException(
                    "当前账号缺少快捷栏持久化记录："
                            + accountUuid);
        }

        Long quickSlotId = quickSlot.getId();

        if (quickSlotId == null || quickSlotId <= 0L)
        {
            throw new IllegalStateException(
                    "服务端快捷栏持久化 ID 无效："
                            + accountUuid);
        }

        List<PlayerQuickSlotBinding> bindingRows =
                bindingMapper.selectByQuickSlotId(
                        quickSlotId);

        if (bindingRows == null)
        {
            throw new IllegalStateException(
                    "快捷栏绑定查询返回了 null 集合");
        }

        int invalidOwnershipCount =
                bindingMapper.countBindingsOutsideOwnerWarehouse(
                        quickSlotId);

        if (invalidOwnershipCount != 0)
        {
            throw new IllegalStateException(
                    "快捷栏包含不属于当前账号仓库的物品绑定："
                            + invalidOwnershipCount);
        }

        try
        {
            return resolveSnapshot(
                    quickSlot,
                    bindingRows);
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalStateException(
                    "服务端快捷栏持久化数据损坏："
                            + accountUuid,
                    exception);
        }
    }

    private static ResolvedQuickSlotSnapshot resolveSnapshot(
            PlayerQuickSlot quickSlot,
            List<PlayerQuickSlotBinding> bindingRows)
    {
        long quickSlotId = requireField(
                quickSlot.getId(),
                "quickSlot.id");

        long revision = requireField(
                quickSlot.getRevision(),
                "quickSlot.revision");

        int schemaVersion = requireField(
                quickSlot.getSchemaVersion(),
                "quickSlot.schemaVersion");

        List<ResolvedQuickSlotBinding> bindings =
                new ArrayList<>(bindingRows.size());

        for (PlayerQuickSlotBinding row : bindingRows)
        {
            if (row == null)
            {
                throw new IllegalArgumentException(
                        "bindingRows 不能包含 null");
            }

            long rowQuickSlotId = requireField(
                    row.getQuickSlotId(),
                    "binding.quickSlotId");

            if (rowQuickSlotId != quickSlotId)
            {
                throw new IllegalArgumentException(
                        "绑定所属快捷栏与查询目标不一致");
            }

            int slotIndex = requireField(
                    row.getSlotIndex(),
                    "binding.slotIndex");

            UUID itemInstanceId = parseCanonicalUuid(
                    requireField(
                            row.getItemInstanceUuid(),
                            "binding.itemInstanceUuid"));

            bindings.add(
                    new ResolvedQuickSlotBinding(
                            slotIndex,
                            itemInstanceId));
        }

        return new ResolvedQuickSlotSnapshot(
                quickSlotId,
                revision,
                schemaVersion,
                bindings);
    }

    /**
     * UUID.fromString 可能接受部分非规范写法，
     * 因此解析后还要反向比较标准字符串。
     */
    private static UUID parseCanonicalUuid(String value)
    {
        try
        {
            UUID parsed = UUID.fromString(value);

            if (!parsed.toString().equalsIgnoreCase(value))
            {
                throw new IllegalArgumentException(
                        "物品实例 ID 不是规范 UUID");
            }

            return parsed;
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalArgumentException(
                    "物品实例 ID 不是有效的规范 UUID",
                    exception);
        }
    }

    private static <T> T requireField(
            T value,
            String fieldName)
    {
        if (value == null)
        {
            throw new IllegalArgumentException(
                    fieldName + " 不能为空");
        }

        return value;
    }
}