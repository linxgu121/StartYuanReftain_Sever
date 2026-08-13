package cn.niuma.lingdi000721.startyuanreftain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;

/**
 * 物品 Footprint 中的一个局部逻辑格
 */
@Getter
@TableName("item_defintion_id")
public class ItemFootprintCell {
    @TableField("item_definition_id")
    private String itemDefinitionId;

    @TableField("local_x")
    private Integer localX;

    @TableField("local_y")
    private Integer localY;

    /**
     * 供 MyBatis 通过反射创建实体。
     */
    public ItemFootprintCell()
    {
    }
}
