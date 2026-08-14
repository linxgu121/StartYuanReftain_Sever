package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;


import cn.niuma.lingdi000721.startyuanreftain.enums.ItemRotationPolicy;
import cn.niuma.lingdi000721.startyuanreftain.service.item.ResolvedItemDefinition;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.spatial.WarehouseCellCoordinate;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.spatial.WarehousePlacementFootprintUtility;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 验证从数据库加载的仓库快照是否满足服务端权威规则。
 *
 * 当前检查：
 * 物品定义、堆叠数量、旋转策略、仓库边界和格子碰撞。
 */
@Component
public class WarehouseSnapshotValidator
{
    public void validate(
            ResolvedWarehouseSnapshot snapshot,
            Map<String, ResolvedItemDefinition> definitionsById)
    {
        Objects.requireNonNull(
                snapshot,
                "snapshot 不能为空");

        Objects.requireNonNull(
                definitionsById,
                "definitionsById 不能为空");

        Set<WarehouseCellCoordinate> occupiedCells =
                new HashSet<>();

        for (ResolvedWarehousePlacement placement :
                snapshot.placements())
        {
            ResolvedItemDefinition definition =
                    requireDefinition(
                            placement,
                            definitionsById);

            validatePlacementRules(
                    placement,
                    definition);

            List<WarehouseCellCoordinate> placementCells;

            try
            {
                placementCells =
                        WarehousePlacementFootprintUtility
                                .resolveOccupiedCells(
                                        definition.footprint(),
                                        placement.orientationDegrees(),
                                        placement.originX(),
                                        placement.originY(),
                                        snapshot.header().width(),
                                        snapshot.header().height());
            }
            catch (IllegalArgumentException exception)
            {
                throw new IllegalStateException(
                        "仓库物品空间数据无效，instanceId："
                                + placement.instanceId(),
                        exception);
            }

            for (WarehouseCellCoordinate cell :
                    placementCells)
            {
                if (!occupiedCells.add(cell))
                {
                    throw new IllegalStateException(
                            "仓库物品发生空间碰撞，instanceId："
                                    + placement.instanceId()
                                    + "，逻辑格："
                                    + cell);
                }
            }
        }
    }

    private static ResolvedItemDefinition requireDefinition(
            ResolvedWarehousePlacement placement,
            Map<String, ResolvedItemDefinition> definitionsById)
    {
        ResolvedItemDefinition definition =
                definitionsById.get(
                        placement.itemDefinitionId());

        if (definition == null)
        {
            throw new IllegalStateException(
                    "仓库物品缺少启用中的定义："
                            + placement.itemDefinitionId());
        }

        if (!placement.itemDefinitionId().equals(
                definition.itemDefinitionId()))
        {
            throw new IllegalStateException(
                    "仓库物品定义与 Placement 不一致："
                            + placement.itemDefinitionId());
        }

        return definition;
    }

    private static void validatePlacementRules(
            ResolvedWarehousePlacement placement,
            ResolvedItemDefinition definition)
    {
        if (placement.count() >
                definition.maxStackCount())
        {
            throw new IllegalStateException(
                    "仓库物品数量超过最大堆叠数量，instanceId："
                            + placement.instanceId());
        }

        if (definition.rotationPolicy() ==
                ItemRotationPolicy.FIXED &&
                placement.orientationDegrees() != 0)
        {
            throw new IllegalStateException(
                    "不可旋转物品使用了非零方向，instanceId："
                            + placement.instanceId());
        }
    }
}
