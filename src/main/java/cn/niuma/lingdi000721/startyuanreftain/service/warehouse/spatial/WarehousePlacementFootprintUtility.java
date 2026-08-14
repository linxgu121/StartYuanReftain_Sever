package cn.niuma.lingdi000721.startyuanreftain.service.warehouse.spatial;


import cn.niuma.lingdi000721.startyuanreftain.service.item.ItemFootprintCoordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * 把物品的局部 Footprint 转换成仓库绝对占用格。
 *
 * 当前只处理旋转、平移和边界，不处理格子碰撞。
 */
public final class WarehousePlacementFootprintUtility {
    private WarehousePlacementFootprintUtility()
    {
    }

    public static List<WarehouseCellCoordinate> resolveOccupiedCells(
            List<ItemFootprintCoordinate> footprint,
            int orientationDegrees,
            int originX,
            int originY,
            int containerWidth,
            int containerHeight)
    {
        if (originX < 0 || originY < 0)
        {
            throw new IllegalArgumentException(
                    "Placement 原点不能为负数");
        }

        if (containerWidth <= 0 || containerHeight <= 0)
        {
            throw new IllegalArgumentException(
                    "容器尺寸必须大于 0");
        }

        List<ItemFootprintCoordinate> rotatedFootprint =
                ItemFootprintUtility.rotate(
                        footprint,
                        orientationDegrees);

        List<WarehouseCellCoordinate> occupiedCells =
                new ArrayList<>(rotatedFootprint.size());

        for (ItemFootprintCoordinate localCell : rotatedFootprint)
        {
            /*
             * 先使用 long 计算，避免两个 int 相加时发生溢出。
             */
            long worldX =
                    (long) originX + localCell.x();

            long worldY =
                    (long) originY + localCell.y();

            if (worldX >= containerWidth ||
                    worldY >= containerHeight)
            {
                throw new IllegalArgumentException(
                        "物品占用格超出容器范围：("
                                + worldX + ", "
                                + worldY + ")");
            }

            occupiedCells.add(
                    new WarehouseCellCoordinate(
                            (int) worldX,
                            (int) worldY));
        }

        return List.copyOf(occupiedCells);
    }
}
