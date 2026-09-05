package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.common.error.CommonErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerQuickSlotBinding;
import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerWarehouse;
import cn.niuma.lingdi000721.startyuanreftain.enums.WarehouseErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerQuickSlotBindingMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerQuickSlotMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerWarehouseMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.WarehouseItemMapper;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.QuickSlotBindingCommand;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.SaveQuickSlotSnapshotCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 完整替换玩家快捷栏持久化快照。
 *
 * 本服务只处理持久化绑定、所有权和并发版本，
 * 不计算角色快捷栏容量、当前选中槽位或物品能否在战斗中使用。
 */
@Service
public class QuickSlotSaveService
{
    private final QuickSlotSnapshotService snapshotService;
    private final PlayerQuickSlotMapper quickSlotMapper;
    private final PlayerQuickSlotBindingMapper bindingMapper;
    private final PlayerWarehouseMapper warehouseMapper;
    private final WarehouseItemMapper warehouseItemMapper;

    public QuickSlotSaveService(
            QuickSlotSnapshotService snapshotService,
            PlayerQuickSlotMapper quickSlotMapper,
            PlayerQuickSlotBindingMapper bindingMapper,
            PlayerWarehouseMapper warehouseMapper,
            WarehouseItemMapper warehouseItemMapper)
    {
        this.snapshotService = Objects.requireNonNull(
                snapshotService,
                "snapshotService 不能为空");

        this.quickSlotMapper = Objects.requireNonNull(
                quickSlotMapper,
                "quickSlotMapper 不能为空");

        this.bindingMapper = Objects.requireNonNull(
                bindingMapper,
                "bindingMapper 不能为空");

        this.warehouseMapper = Objects.requireNonNull(
                warehouseMapper,
                "warehouseMapper 不能为空");

        this.warehouseItemMapper = Objects.requireNonNull(
                warehouseItemMapper,
                "warehouseItemMapper 不能为空");
    }

    /**
     * 使用完整目标配置替换当前快捷栏，并返回新的权威快照。
     */
    @Transactional
    public ResolvedQuickSlotSnapshot save(
            SaveQuickSlotSnapshotCommand command)
    {
        Objects.requireNonNull(
                command,
                "command 不能为空");

        ResolvedQuickSlotSnapshot currentSnapshot =
                snapshotService.loadRequiredByAccountUuid(
                        command.accountUuid());

        verifySchemaVersion(
                currentSnapshot,
                command.schemaVersion());

        verifyExpectedRevision(
                currentSnapshot,
                command.expectedRevision());

        List<ResolvedQuickSlotBinding> desiredBindings =
                resolveDesiredBindings(command.bindings());

        /*
         * Unity 可以提交显式空槽，但数据库不保存空槽。
         * 因此先规范化，再判断是否确实发生变化。
         */
        if (currentSnapshot.bindings().equals(desiredBindings))
        {
            return currentSnapshot;
        }

        verifyItemOwnership(
                command.accountUuid(),
                desiredBindings);

        /*
         * CAS 必须在删除旧绑定之前执行。
         * 如果版本已经变化，事务会在破坏当前配置前直接失败。
         */
        int updatedRevisionRows =
                quickSlotMapper.incrementRevisionIfMatches(
                        currentSnapshot.persistenceId(),
                        command.expectedRevision());

        verifyRevisionUpdate(updatedRevisionRows);

        int deletedRows =
                bindingMapper.deleteByQuickSlotId(
                        currentSnapshot.persistenceId());

        verifyDeletedRows(
                deletedRows,
                currentSnapshot.bindings().size());

        if (!desiredBindings.isEmpty())
        {
            List<PlayerQuickSlotBinding> bindingRows =
                    createBindingRows(
                            currentSnapshot.persistenceId(),
                            desiredBindings);

            int insertedRows =
                    bindingMapper.insertAll(bindingRows);

            verifyInsertedRows(
                    insertedRows,
                    bindingRows.size());
        }

        /*
         * 重新读取，确保返回给 Unity 的 revision 和绑定内容
         * 都来自事务内最终落库的权威状态。
         */
        return snapshotService.loadRequiredByAccountUuid(
                command.accountUuid());
    }

    /**
     * 移除显式空槽，并转换成只能表示非空持久化绑定的领域模型。
     */
    private static List<ResolvedQuickSlotBinding>
    resolveDesiredBindings(
            List<QuickSlotBindingCommand> requestedBindings)
    {
        List<ResolvedQuickSlotBinding> resolvedBindings =
                new ArrayList<>(requestedBindings.size());

        for (QuickSlotBindingCommand binding : requestedBindings)
        {
            if (!binding.hasItem())
            {
                continue;
            }

            resolvedBindings.add(
                    new ResolvedQuickSlotBinding(
                            binding.slotIndex(),
                            binding.itemInstanceId()));
        }

        /*
         * command 已经按槽位排序，所以过滤空槽后仍然有序。
         * 返回不可变集合，防止事务执行期间被修改。
         */
        return List.copyOf(resolvedBindings);
    }

    /**
     * 批量验证所有非空实例确实属于当前账号的玩家仓库。
     */
    private void verifyItemOwnership(
            UUID accountUuid,
            List<ResolvedQuickSlotBinding> bindings)
    {
        List<String> distinctInstanceUuids =
                bindings.stream()
                        .map(ResolvedQuickSlotBinding::itemInstanceId)
                        /*
                         * 同一物品可以持久化到多个槽位，
                         * 所有权查询只需要检查一次。
                         */
                        .distinct()
                        .map(UUID::toString)
                        .toList();

        if (distinctInstanceUuids.isEmpty())
        {
            return;
        }

        long warehouseId =
                loadRequiredWarehouseId(accountUuid);

        int ownedInstanceCount =
                warehouseItemMapper.countOwnedInstances(
                        warehouseId,
                        distinctInstanceUuids);

        if (ownedInstanceCount != distinctInstanceUuids.size())
        {
            /*
             * 不区分“物品不存在”和“物品属于其他账号”，
             * 防止通过接口探测其他玩家的物品实例。
             */
            throw new BusinessException(
                    WarehouseErrorCode.ITEM_NOT_FOUND);
        }
    }

    private long loadRequiredWarehouseId(UUID accountUuid)
    {
        PlayerWarehouse warehouse =
                warehouseMapper.selectByAccountUuid(
                        accountUuid.toString());

        if (warehouse == null)
        {
            throw new IllegalStateException(
                    "当前账号缺少玩家仓库持久化记录："
                            + accountUuid);
        }

        Long warehouseId = warehouse.getId();

        if (warehouseId == null || warehouseId <= 0L)
        {
            throw new IllegalStateException(
                    "当前账号的玩家仓库 ID 无效："
                            + accountUuid);
        }

        return warehouseId;
    }

    private static List<PlayerQuickSlotBinding>
    createBindingRows(
            long quickSlotId,
            List<ResolvedQuickSlotBinding> bindings)
    {
        List<PlayerQuickSlotBinding> rows =
                new ArrayList<>(bindings.size());

        for (ResolvedQuickSlotBinding binding : bindings)
        {
            rows.add(
                    new PlayerQuickSlotBinding(
                            quickSlotId,
                            binding.slotIndex(),
                            binding.itemInstanceId().toString()));
        }

        return rows;
    }

    private static void verifySchemaVersion(
            ResolvedQuickSlotSnapshot currentSnapshot,
            int requestedSchemaVersion)
    {
        if (currentSnapshot.schemaVersion()
                != requestedSchemaVersion)
        {
            throw new BusinessException(
                    CommonErrorCode.INVALID_REQUEST,
                    "快捷栏协议版本与当前持久化版本不一致");
        }
    }

    private static void verifyExpectedRevision(
            ResolvedQuickSlotSnapshot currentSnapshot,
            long expectedRevision)
    {
        if (currentSnapshot.revision() != expectedRevision)
        {
            throw new BusinessException(
                    WarehouseErrorCode.REVISION_CONFLICT);
        }
    }

    private static void verifyRevisionUpdate(int updatedRows)
    {
        if (updatedRows == 0)
        {
            throw new BusinessException(
                    WarehouseErrorCode.REVISION_CONFLICT);
        }

        if (updatedRows != 1)
        {
            throw new IllegalStateException(
                    "快捷栏 revision 更新行数异常："
                            + updatedRows);
        }
    }

    private static void verifyDeletedRows(
            int deletedRows,
            int expectedRows)
    {
        if (deletedRows != expectedRows)
        {
            throw new IllegalStateException(
                    "快捷栏旧绑定删除行数异常：预期 "
                            + expectedRows
                            + "，实际 "
                            + deletedRows);
        }
    }

    private static void verifyInsertedRows(
            int insertedRows,
            int expectedRows)
    {
        if (insertedRows != expectedRows)
        {
            throw new IllegalStateException(
                    "快捷栏新绑定插入行数异常：预期 "
                            + expectedRows
                            + "，实际 "
                            + insertedRows);
        }
    }
}