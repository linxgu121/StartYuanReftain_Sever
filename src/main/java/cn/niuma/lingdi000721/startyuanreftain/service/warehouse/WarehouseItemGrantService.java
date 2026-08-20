package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.enums.WarehouseErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerWarehouseMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.WarehouseItemMapper;
import cn.niuma.lingdi000721.startyuanreftain.service.item.ItemCatalogService;
import cn.niuma.lingdi000721.startyuanreftain.service.item.ResolvedItemDefinition;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.GrantWarehouseItemCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 仓库物品发放事务服务。
 *
 * 负责加载权威快照、检查物品定义、寻找合法位置、
 * 插入物品记录并通过 revision CAS 处理并发冲突。
 */
@Service
public class WarehouseItemGrantService
{
    private final WarehouseSnapshotService snapshotService;
    private final ItemCatalogService itemCatalogService;
    private final WarehouseItemGrantCandidateFactory candidateFactory;
    private final WarehouseItemMapper warehouseItemMapper;
    private final PlayerWarehouseMapper playerWarehouseMapper;

    public WarehouseItemGrantService(
            WarehouseSnapshotService snapshotService,
            ItemCatalogService itemCatalogService,
            WarehouseItemGrantCandidateFactory candidateFactory,
            WarehouseItemMapper warehouseItemMapper,
            PlayerWarehouseMapper playerWarehouseMapper)
    {
        this.snapshotService = Objects.requireNonNull(
                snapshotService,
                "snapshotService 不能为空");

        this.itemCatalogService = Objects.requireNonNull(
                itemCatalogService,
                "itemCatalogService 不能为空");

        this.candidateFactory = Objects.requireNonNull(
                candidateFactory,
                "candidateFactory 不能为空");

        this.warehouseItemMapper = Objects.requireNonNull(
                warehouseItemMapper,
                "warehouseItemMapper 不能为空");

        this.playerWarehouseMapper = Objects.requireNonNull(
                playerWarehouseMapper,
                "playerWarehouseMapper 不能为空");
    }

    @Transactional
    public ResolvedWarehouseSnapshot grant(GrantWarehouseItemCommand command)
    {
        Objects.requireNonNull(command,"command不能为空");

        //根据账号UUID加载玩家当前仓库快照，找不到直接抛异常
        ResolvedWarehouseSnapshot currentSnapshot = snapshotService.loadRequiredByAccountUuid(command.accountUuid());

        //根据物品定义ID查询启用的配表，不存在抛业务异常
        ResolvedItemDefinition grantedDefinition =
                itemCatalogService
                        .findEnabledById(command.itemDefinitionId())
                        .orElseThrow(() -> new BusinessException(WarehouseErrorCode.ITEM_DEFINITION_NOT_FOUND));

        verifyGrantedCount(command.count(), grantedDefinition);

        /*
         * 候选快照验证需要当前仓库内全部物品的定义，
         * 同时也需要本次准备发放的物品定义。
         */
        Map<String, ResolvedItemDefinition> definitionsById = loadCandidateDefinitions(currentSnapshot, grantedDefinition);

        /*
         * InstanceId 只能由服务端生成，
         * 客户端不能指定数据库中的物品实例身份。
         */
        UUID instanceId = UUID.randomUUID();

        ResolvedWarehouseSnapshot candidateSnapshot =
                candidateFactory.createFirstValidCandidate(
                        currentSnapshot,
                        grantedDefinition,
                        definitionsById,
                        instanceId,
                        command.count());

        ResolvedWarehousePlacement grantedPlacement =
                requireGrantedPlacement(
                        candidateSnapshot,
                        instanceId);

        long warehouseId = currentSnapshot.header().persistenceId();

        int insertedRows = warehouseItemMapper.insertItem(
                        warehouseId,
                        instanceId.toString(),
                        grantedDefinition.itemDefinitionId(),
                        command.count(),
                        grantedPlacement.originX(),
                        grantedPlacement.originY(),
                        grantedPlacement.orientationDegrees());

        verifyInsertResult(insertedRows);

        /*
         * 使用加载快照时的 revision 执行 CAS。
         *
         * 如果期间其他事务修改了仓库，这里的更新将返回 0，
         * 随后抛出的 RuntimeException 会让前面的 INSERT 一起回滚。
         */
        int updatedRevisionRows = playerWarehouseMapper.incrementRevisionIfMatches(
                        warehouseId,
                        currentSnapshot.header().revision());

        verifyRevisionUpdate(updatedRevisionRows);

        /*
         * 使用加载快照时的 revision 执行 CAS。
         *
         * 如果期间其他事务修改了仓库，这里的更新将返回 0，
         * 随后抛出的 RuntimeException 会让前面的 INSERT 一起回滚。
         */
        return snapshotService.loadRequiredByAccountUuid(command.accountUuid());
    }

    /**
     * 发放道具前校验**发放数量不能超过物品的最大堆叠数**
     */
    private void verifyGrantedCount(
            int count,
            ResolvedItemDefinition definition)
    {
        if (count > definition.maxStackCount())
        {
            throw new BusinessException(WarehouseErrorCode.INVALID_ITEM_COUNT);
        }
    }

    /**
     * 批量加载本次业务需要用到的全部物品配表定义
     */
    private Map<String, ResolvedItemDefinition> loadCandidateDefinitions(
            ResolvedWarehouseSnapshot currentSnapshot,
            ResolvedItemDefinition grantedDefinition)
    {
        List<String> definitionIds =
                new ArrayList<>(currentSnapshot.placements().size() + 1);

        for (ResolvedWarehousePlacement placement : currentSnapshot.placements())
        {
            definitionIds.add(placement.itemDefinitionId());
        }

        definitionIds.add(grantedDefinition.itemDefinitionId());

        return itemCatalogService.loadEnabledRequiredByIds(definitionIds);
    }

    /**
     * 经过工厂组装后的候选快照
     */
    private ResolvedWarehousePlacement requireGrantedPlacement(
            ResolvedWarehouseSnapshot candidateSnapshot,
            UUID instanceId)
    {
        return candidateSnapshot.placements()
                .stream()
                .filter(
                        placement ->
                                placement.instanceId()
                                        .equals(instanceId))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "候选快照缺少新发放的物品实例"));
    }

    /**
     * 校验插入结果
     */
    private void verifyInsertResult(int insertedRows)
    {
        if (insertedRows != 1)
        {
            throw new IllegalStateException(
                    "仓库物品插入行数异常："
                            + insertedRows);
        }
    }

    /**
     * 版本号乐观锁校验
     */
    private void verifyRevisionUpdate(int updatedRows)
    {
        if (updatedRows == 0)
        {
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
