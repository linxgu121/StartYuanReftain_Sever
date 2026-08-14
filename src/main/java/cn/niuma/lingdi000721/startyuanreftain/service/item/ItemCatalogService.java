package cn.niuma.lingdi000721.startyuanreftain.service.item;


import cn.niuma.lingdi000721.startyuanreftain.entity.ItemDefinition;
import cn.niuma.lingdi000721.startyuanreftain.entity.ItemFootprintCell;
import cn.niuma.lingdi000721.startyuanreftain.mapper.ItemDefinitionMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.ItemFootprintCellMapper;
import cn.niuma.lingdi000721.startyuanreftain.mapper.ItemCatalogMetadataMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * 从服务端权威目录加载并校验完整物品定义
 */
@Service
public class ItemCatalogService {
    private final ItemDefinitionMapper definitionMapper;
    private final ItemFootprintCellMapper footprintCellMapper;
    private final ItemCatalogMetadataMapper metadataMapper;

    public ItemCatalogService(
            ItemDefinitionMapper definitionMapper,
            ItemFootprintCellMapper footprintCellMapper,
            ItemCatalogMetadataMapper metadataMapper)
    {
        this.definitionMapper = Objects.requireNonNull(
                definitionMapper,
                "definitionMapper 不能为空");

        this.footprintCellMapper = Objects.requireNonNull(
                footprintCellMapper,
                "footprintCellMapper 不能为空");

        this.metadataMapper = Objects.requireNonNull(
                metadataMapper,
                "metadataMapper 不能为空");
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
            /*
             * 先验证定义行，再查询 Footprint。
             * 定义行已损坏时不继续执行无意义的第二次查询。
             */
            validateDefinitionRow(
                    itemDefinitionId,
                    definition);

            List<ItemFootprintCell> cellRows =
                    footprintCellMapper.selectByDefinitionId(
                            itemDefinitionId);

            return Optional.of(
                    resolveValidatedDefinition(
                            definition,
                            cellRows));
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalStateException("服务端物品目录定义损坏：" + itemDefinitionId, exception);
        }
    }

    /**
     * 批量加载调用者要求的全部启用物品定义。
     *
     * 输入会先排序和去重。只要任一请求定义缺失、停用或损坏，
     * 整批加载都会失败，不返回不完整的目录结果。
     */
    @Transactional(readOnly = true)
    public Map<String, ResolvedItemDefinition>
    loadEnabledRequiredByIds(
            List<String> itemDefinitionIds)
    {
        Objects.requireNonNull(
                itemDefinitionIds,
                "itemDefinitionIds 不能为空");

        Set<String> uniqueIds =
                new TreeSet<>();

        for (String itemDefinitionId : itemDefinitionIds)
        {
            Objects.requireNonNull(
                    itemDefinitionId,
                    "itemDefinitionId 不能为空");

            if (itemDefinitionId.isBlank())
            {
                throw new IllegalArgumentException(
                        "itemDefinitionId 不能为空");
            }

            uniqueIds.add(itemDefinitionId);
        }

        if (uniqueIds.isEmpty())
        {
            return Map.of();
        }

        List<String> requestedIds =
                List.copyOf(uniqueIds);

        List<ItemDefinition> definitionRows =
                definitionMapper.selectEnabledByIds(
                        requestedIds);

        List<ItemFootprintCell> footprintRows =
                footprintCellMapper.selectByDefinitionIds(
                        requestedIds);

        try
        {
            return resolveDefinitions(
                    requestedIds,
                    definitionRows,
                    footprintRows);
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalStateException(
                    "服务端物品目录批量数据损坏",
                    exception);
        }
    }

    /**
     * 读取服务端权威物品目录版本。
     */
    @Transactional(readOnly = true)
    public int getCurrentCatalogVersion()
    {
        Integer catalogVersion =
                metadataMapper.selectMainCatalogVersion();

        if (catalogVersion == null)
        {
            throw new IllegalStateException(
                    "服务端缺少 MAIN 物品目录版本");
        }

        if (catalogVersion < 1)
        {
            throw new IllegalStateException(
                    "服务端 MAIN 物品目录版本无效");
        }

        return catalogVersion;
    }

    private void validateDefinitionRow(
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
    }

    private ResolvedItemDefinition resolveValidatedDefinition(
            ItemDefinition definition,
            List<ItemFootprintCell> cellRows)
    {
        if (cellRows == null)
        {
            throw new IllegalArgumentException("Footprint 查询结果不能为空");
        }

        String actualDefinitionId =
                definition.getItemDefinitionId();

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
                definition.getMaxStackCount(),
                definition.getRotationPolicy(),
                definition.getDefinitionVersion(),
                coordinates);
    }

    private Map<String, ResolvedItemDefinition>
    resolveDefinitions(
            List<String> requestedIds,
            List<ItemDefinition> definitionRows,
            List<ItemFootprintCell> footprintRows)
    {
        if (definitionRows == null)
        {
            throw new IllegalArgumentException(
                    "物品定义查询结果不能为空");
        }

        if (footprintRows == null)
        {
            throw new IllegalArgumentException(
                    "Footprint 查询结果不能为空");
        }

        Set<String> requestedIdSet =
                Set.copyOf(requestedIds);

        Map<String, ItemDefinition> definitionsById =
                new HashMap<>();

        for (ItemDefinition definition : definitionRows)
        {
            if (definition == null)
            {
                throw new IllegalArgumentException(
                        "物品定义查询结果不能包含空行");
            }

            String definitionId =
                    definition.getItemDefinitionId();

            if (definitionId == null)
            {
                throw new IllegalArgumentException(
                        "物品定义 ID 不能为空");
            }

            if (!requestedIdSet.contains(definitionId))
            {
                throw new IllegalArgumentException(
                        "查询结果包含未请求的物品定义");
            }

            if (definitionsById.put(
                    definitionId,
                    definition) != null)
            {
                throw new IllegalArgumentException(
                        "查询结果包含重复的物品定义");
            }

            validateDefinitionRow(
                    definitionId,
                    definition);
        }

        if (!definitionsById.keySet().equals(requestedIdSet))
        {
            Set<String> missingIds =
                    new TreeSet<>(requestedIdSet);

            missingIds.removeAll(definitionsById.keySet());

            throw new IllegalArgumentException(
                    "缺少启用中的物品定义：" + missingIds);
        }

        Map<String, List<ItemFootprintCell>>
                cellsByDefinitionId = new HashMap<>();

        for (ItemFootprintCell cell : footprintRows)
        {
            if (cell == null)
            {
                throw new IllegalArgumentException(
                        "Footprint 不能包含空行");
            }

            String definitionId =
                    cell.getItemDefinitionId();

            if (definitionId == null)
            {
                throw new IllegalArgumentException(
                        "Footprint 的物品定义 ID 不能为空");
            }

            if (!requestedIdSet.contains(definitionId))
            {
                throw new IllegalArgumentException(
                        "Footprint 行属于未请求的物品定义");
            }

            cellsByDefinitionId
                    .computeIfAbsent(
                            definitionId,
                            ignored -> new ArrayList<>())
                    .add(cell);
        }

        Map<String, ResolvedItemDefinition> resolved =
                new LinkedHashMap<>();

        /*
         * 按已经排序的请求 ID 组装，避免依赖 Mapper 返回顺序，
         * 同时让 Map 的迭代顺序稳定。
         */
        for (String definitionId : requestedIds)
        {
            ResolvedItemDefinition resolvedDefinition =
                    resolveValidatedDefinition(
                            definitionsById.get(definitionId),
                            cellsByDefinitionId.getOrDefault(
                                    definitionId,
                                    List.of()));

            resolved.put(
                    definitionId,
                    resolvedDefinition);
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(resolved));
    }
}
