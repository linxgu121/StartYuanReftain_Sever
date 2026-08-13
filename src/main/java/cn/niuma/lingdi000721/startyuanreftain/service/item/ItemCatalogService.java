package cn.niuma.lingdi000721.startyuanreftain.service.item;


import cn.niuma.lingdi000721.startyuanreftain.entity.ItemDefinition;
import cn.niuma.lingdi000721.startyuanreftain.entity.ItemFootprintCell;
import cn.niuma.lingdi000721.startyuanreftain.mapper.ItemDefinitionMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.ItemFootprintCellMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 从服务端权威目录加载并校验完整物品定义
 */
@Service
public class ItemCatalogService {
    private final ItemDefinitionMapper definitionMapper;
    private final ItemFootprintCellMapper footprintCellMapper;

    public ItemCatalogService(
            ItemDefinitionMapper definitionMapper,
            ItemFootprintCellMapper footprintCellMapper)
    {
        this.definitionMapper = Objects.requireNonNull(
                definitionMapper,
                "definitionMapper 不能为空");

        this.footprintCellMapper = Objects.requireNonNull(
                footprintCellMapper,
                "footprintCellMapper 不能为空");
    }

    /**
     * 查询启用中的物品定义。
     *
     * 不存在或已停用时返回 Optional.empty()；
     * 权威目录存在损坏时抛出 IllegalStateException。
     */
    @Transactional(readOnly = true)
    public Optional<ResolvedItemDefinition> findEnabledById(String itemDefinitionId)
    {
        Objects.requireNonNull(
                itemDefinitionId,
                "itemDefinitionId 不能为空");

        if (itemDefinitionId.isBlank())
        {
            throw new IllegalArgumentException("itemDefinitionId 不能为空");
        }

        ItemDefinition definition = definitionMapper.selectEnabledById(itemDefinitionId);

        if (definition == null)
        {
            return Optional.empty();
        }

        try
        {
            return Optional.of(
                    resolveDefinition(
                            itemDefinitionId,
                            definition));
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalStateException("服务端物品目录定义损坏：" + itemDefinitionId, exception);
        }
    }

    private ResolvedItemDefinition resolveDefinition(
            String requestedDefinitionId,
            ItemDefinition definition)
    {
        String actualDefinitionId = definition.getItemDefinitionId();

        if (!requestedDefinitionId.equals(actualDefinitionId))
        {
            throw new IllegalArgumentException("查询结果的物品 ID 与请求不一致");
        }

        if (!Boolean.TRUE.equals(definition.getEnabled()))
        {
            throw new IllegalArgumentException("查询结果不是启用中的物品定义");
        }

        Integer maxStackCount = definition.getMaxStackCount();

        Integer definitionVersion = definition.getDefinitionVersion();

        if (maxStackCount == null)
        {
            throw new IllegalArgumentException("maxStackCount 不能为空");
        }

        if (definitionVersion == null)
        {
            throw new IllegalArgumentException("definitionVersion 不能为空");
        }

        List<ItemFootprintCell> cellRows =
                footprintCellMapper.selectByDefinitionId(
                        actualDefinitionId);

        if (cellRows == null)
        {
            throw new IllegalArgumentException("Footprint 查询结果不能为空");
        }

        List<ItemFootprintCoordinate> coordinates =
                new ArrayList<>(cellRows.size());

        for (ItemFootprintCell cell : cellRows)
        {
            if (cell == null)
            {
                throw new IllegalArgumentException("Footprint 不能包含空行");
            }

            if (!actualDefinitionId.equals(
                    cell.getItemDefinitionId()))
            {
                throw new IllegalArgumentException("Footprint 行属于其他物品定义");
            }

            Integer localX = cell.getLocalX();
            Integer localY = cell.getLocalY();

            if (localX == null || localY == null)
            {
                throw new IllegalArgumentException("Footprint 坐标不能为空");
            }

            coordinates.add(
                    new ItemFootprintCoordinate(
                            localX,
                            localY));
        }

        return new ResolvedItemDefinition(
                actualDefinitionId,
                definition.getItemType(),
                definition.getStackPolicy(),
                maxStackCount,
                definition.getRotationPolicy(),
                definitionVersion,
                coordinates);
    }
}
