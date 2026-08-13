package cn.niuma.lingdi000721.startyuanreftain.service.item;

/**
 * 物品 Footprint 中不可变的局部逻辑格坐标。
 */
public record ItemFootprintCoordinate(int x,int y)
{
    public ItemFootprintCoordinate
    {
        if (x < 0 || y < 0)
        {
            throw new IllegalArgumentException("Footprint 坐标不能为负数");
        }
    }
}
