package cn.niuma.lingdi000721.startyuanreftain.service.item;

import cn.niuma.lingdi000721.startyuanreftain.enums.ItemRotationPolicy;
import cn.niuma.lingdi000721.startyuanreftain.enums.ItemStackPolicy;
import cn.niuma.lingdi000721.startyuanreftain.enums.ItemType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 已从数据库完整加载并通过领域校验的物品定义
 */
public record ResolvedItemDefinition(
        String itemDefinitionId,
        ItemType itemType,
        ItemStackPolicy stackPolicy,
        int maxStackCount,
        ItemRotationPolicy rotationPolicy,
        int definitionVersion,
        List<ItemFootprintCoordinate> footprint)
{
    public ResolvedItemDefinition
    {
        itemDefinitionId = requireValue(
                itemDefinitionId,
                "itemDefinitionId");

        itemType = requireValue(
                itemType,
                "itemType");

        stackPolicy = requireValue(
                stackPolicy,
                "stackPolicy");

        rotationPolicy = requireValue(
                rotationPolicy,
                "rotationPolicy");

        footprint = requireValue(
                footprint,
                "footprint");

        if (itemDefinitionId.isBlank())
        {
            throw new IllegalArgumentException(
                    "itemDefinitionId 不能为空");
        }

        validateStackPolicy(
                stackPolicy,
                maxStackCount);

        if (definitionVersion < 1)
        {
            throw new IllegalArgumentException("definitionVersion 必须大于等于 1");
        }

        if (footprint.isEmpty())
        {
            throw new IllegalArgumentException("Footprint 不能为空");
        }

        List<ItemFootprintCoordinate> normalizedCells =
                new ArrayList<>(footprint.size());

        Set<ItemFootprintCoordinate> uniqueCells =
                new HashSet<>();

        int minimumX = Integer.MAX_VALUE;
        int minimumY = Integer.MAX_VALUE;

        for (ItemFootprintCoordinate coordinate : footprint)
        {
            coordinate = requireValue(coordinate, "Footprint 坐标");

            if (!uniqueCells.add(coordinate))
            {
                throw new IllegalArgumentException("Footprint 不能包含重复坐标");
            }

            normalizedCells.add(coordinate);

            minimumX = Math.min(minimumX, coordinate.x());

            minimumY = Math.min(minimumY, coordinate.y());
        }

        /*
         * UE 的规范化规则是分别让最小 X 和最小 Y 归零。
         * 不要求形状中一定存在 (0,0)。
         */
        if (minimumX != 0 || minimumY != 0)
        {
            throw new IllegalArgumentException("Footprint 必须经过规范化");
        }

        /*
         * 与 UE FNiumaItemFootprintUtility::SortCells 一致：
         * 先按照 Y 排序，再按照 X 排序。
         */
        normalizedCells.sort(
                Comparator
                        .comparingInt(
                                ItemFootprintCoordinate::y)
                        .thenComparingInt(
                                ItemFootprintCoordinate::x));

        /*
         * List.copyOf 创建不可修改的防御性副本，
         * 防止调用者在构造完成后篡改领域对象。
         */
        footprint = List.copyOf(normalizedCells);
    }

    private static void validateStackPolicy(
            ItemStackPolicy stackPolicy,
            int maxStackCount)
    {
        switch (stackPolicy)
        {
            case NON_STACKABLE ->
            {
                if (maxStackCount != 1)
                {
                    throw new IllegalArgumentException(
                            "不可堆叠物品的最大数量必须为 1");
                }
            }

            case STACKABLE ->
            {
                if (maxStackCount <= 1)
                {
                    throw new IllegalArgumentException(
                            "可堆叠物品的最大数量必须大于 1");
                }
            }
        }
    }

    private static <T> T requireValue(T value, String name)
    {
        if (value == null)
        {
            throw new IllegalArgumentException(name + " 不能为空");
        }

        return value;
    }
}
