package cn.niuma.lingdi000721.startyuanreftain.service.warehouse.spatial;

/**
 * 仓库中的绝对逻辑格坐标。
 *
 * ItemFootprintCoordinate 表示物品内部的局部坐标；
 * WarehouseCellCoordinate 表示放入仓库后的绝对坐标。
 */
public record WarehouseCellCoordinate(int x, int y)
{
    public WarehouseCellCoordinate
    {
        if (x < 0 || y < 0)
        {
            throw new IllegalArgumentException("仓库格坐标不能为负数");
        }
    }
}
