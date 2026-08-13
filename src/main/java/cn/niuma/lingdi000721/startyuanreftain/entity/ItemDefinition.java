package cn.niuma.lingdi000721.startyuanreftain.entity;

import cn.niuma.lingdi000721.startyuanreftain.enums.ItemRotationPolicy;
import cn.niuma.lingdi000721.startyuanreftain.enums.ItemStackPolicy;
import cn.niuma.lingdi000721.startyuanreftain.enums.ItemType;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 服务端权威物品定义
 */
@Getter
@TableName("item_definition")
public class ItemDefinition
{
    /**
     * 跨客户端、服务端和存档使用的稳定物品 ID。
     */
    @TableId(value = "item_definition_id", type = IdType.INPUT)
    private String itemDefinitionId;

    @TableField("item_type")
    private ItemType itemType;

    @TableField("stack_policy")
    private ItemStackPolicy stackPolicy;

    @TableField("max_stack_count")
    private Integer maxStackCount;

    @TableField("rotation_policy")
    private ItemRotationPolicy rotationPolicy;

    @TableField("definition_version")
    private Integer definitionVersion;

    @TableField("enabled")
    private Boolean enabled;

    @TableField(
            value = "created_at",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(
            value = "updated_at",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;

    /**
     * 供 MyBatis 通过反射创建实体。
     */
    public ItemDefinition()
    {
    }
}
