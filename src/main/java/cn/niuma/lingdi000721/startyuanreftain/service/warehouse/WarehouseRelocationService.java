package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.enums.WarehouseErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerWarehouseMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.WarehouseItemMapper;
import cn.niuma.lingdi000721.startyuanreftain.service.item.ItemCatalogService;
import cn.niuma.lingdi000721.startyuanreftain.service.item.ResolvedItemDefinition;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.RelocateWarehouseItemCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 仓库物品重定位事务服务。
 *
 * 候选布局验证、Placement 更新和 revision CAS
 * 必须处于同一个数据库事务中。
 */
@Service
public class WarehouseRelocationService
{
    private final WarehouseSnapshotService snapshotService;
    private final WarehouseRelocationCandidateFactory candidateFactory;
    private final ItemCatalogService itemCatalogService;
    private final WarehouseSnapshotValidator snapshotValidator;
    private final WarehouseItemMapper warehouseItemMapper;
    private final PlayerWarehouseMapper playerWarehouseMapper;

    public WarehouseRelocationService(
            WarehouseSnapshotService snapshotService,
            WarehouseRelocationCandidateFactory candidateFactory,
            ItemCatalogService itemCatalogService,
            WarehouseSnapshotValidator snapshotValidator,
            WarehouseItemMapper warehouseItemMapper,
            PlayerWarehouseMapper playerWarehouseMapper)
    {
        this.snapshotService = Objects.requireNonNull(
                snapshotService,
                "snapshotService 不能为空");

        this.candidateFactory = Objects.requireNonNull(
                candidateFactory,
                "candidateFactory 不能为空");

        this.itemCatalogService = Objects.requireNonNull(
                itemCatalogService,
                "itemCatalogService 不能为空");

        this.snapshotValidator = Objects.requireNonNull(
                snapshotValidator,
                "snapshotValidator 不能为空");

        this.warehouseItemMapper = Objects.requireNonNull(
                warehouseItemMapper,
                "warehouseItemMapper 不能为空");

        this.playerWarehouseMapper = Objects.requireNonNull(
                playerWarehouseMapper,
                "playerWarehouseMapper 不能为空");
    }

    /**
     * 原子重定位仓库物品，并返回修改后的权威快照。
     */
    @Transactional
    public ResolvedWarehouseSnapshot relocate(
            RelocateWarehouseItemCommand command)
    {
        Objects.requireNonNull(
                command,
                "command 不能为空");

        ResolvedWarehouseSnapshot currentSnapshot =
                snapshotService.loadRequiredByAccountUuid(
                        command.accountUuid());

        verifyExpectedRevision(
                currentSnapshot,
                command.expectedRevision());

        ResolvedWarehouseSnapshot candidateSnapshot =
                candidateFactory.createCandidate(
                        currentSnapshot,
                        command);

        /*
         * 原点与朝向都未变化时属于幂等空操作：
         * 不执行 UPDATE，也不增加 revision。
         */
        if (candidateSnapshot.equals(currentSnapshot))
        {
            return currentSnapshot;
        }

        validateCandidate(candidateSnapshot);

        long warehouseId =
                currentSnapshot.header().persistenceId();

        int updatedPlacementRows =
                warehouseItemMapper.updatePlacement(
                        warehouseId,
                        command.instanceId().toString(),
                        command.originX(),
                        command.originY(),
                        command.orientationDegrees());

        verifyPlacementUpdate(updatedPlacementRows);

        int updatedRevisionRows =
                playerWarehouseMapper.incrementRevisionIfMatches(
                        warehouseId,
                        command.expectedRevision());

        verifyRevisionUpdate(updatedRevisionRows);

        /*
         * 重新从当前事务读取，返回 revision 已增加、
         * Placement 已更新的完整权威快照。
         */
        return snapshotService.loadRequiredByAccountUuid(
                command.accountUuid());
    }

    private void verifyExpectedRevision(
            ResolvedWarehouseSnapshot currentSnapshot,
            long expectedRevision)
    {
        if (currentSnapshot.header().revision()
                != expectedRevision)
        {
            throw new BusinessException(
                    WarehouseErrorCode.REVISION_CONFLICT);
        }
    }

    private void validateCandidate(
            ResolvedWarehouseSnapshot candidateSnapshot)
    {
        List<String> itemDefinitionIds =
                candidateSnapshot.placements()
                        .stream()
                        .map(
                                ResolvedWarehousePlacement
                                        ::itemDefinitionId)
                        .distinct()
                        .toList();

        Map<String, ResolvedItemDefinition> definitionsById =
                itemCatalogService.loadEnabledRequiredByIds(
                        itemDefinitionIds);

        try
        {
            snapshotValidator.validate(
                    candidateSnapshot,
                    definitionsById);
        }
        catch (IllegalStateException exception)
        {
            /*
             * 当前快照在加载时已经通过验证。
             * 候选快照只改变目标位置和朝向，
             * 因此这里的新失败属于本次放置冲突。
             */
            throw new BusinessException(
                    WarehouseErrorCode.PLACEMENT_CONFLICT);
        }
    }

    private void verifyPlacementUpdate(
            int updatedRows)
    {
        if (updatedRows == 0)
        {
            throw new BusinessException(
                    WarehouseErrorCode.ITEM_NOT_FOUND);
        }

        if (updatedRows != 1)
        {
            throw new IllegalStateException(
                    "仓库 Placement 更新行数异常："
                            + updatedRows);
        }
    }

    private void verifyRevisionUpdate(
            int updatedRows)
    {
        if (updatedRows == 0)
        {
            /*
             * 此异常会触发 Spring 事务回滚，
             * 前面已经执行的 Placement UPDATE 也会被撤销。
             */
            throw new BusinessException(
                    WarehouseErrorCode.REVISION_CONFLICT);
        }

        if (updatedRows != 1)
        {
            throw new IllegalStateException(
                    "仓库 revision 更新行数异常："
                            + updatedRows);
        }
    }
}