package cn.niuma.lingdi000721.startyuanreftain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 仓库中的一个持久化物品实例。
 *
 * 这里只映射数据库行，完整空间校验由后续快照领域层负责。
 */
@Getter
@TableName("warehouse_item")
public class WarehouseItem {
    /**
     * 数据库内部主键，不返回给 UE 客户端。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对应 UE 的 InstanceId。
     */
    @TableField(value = "instance_uuid", updateStrategy = FieldStrategy.NEVER)
    private String instanceUuid;

    /**
     * 所属仓库的数据库内部主键。
     */
    @TableField(value = "warehouse_id", updateStrategy = FieldStrategy.NEVER)
    private Long warehouseId;

    /**
     * 服务端权威物品定义 ID。
     */
    @TableField(value = "item_definition_id", updateStrategy = FieldStrategy.NEVER)
    private String itemDefinitionId;

    @TableField("stack_count")
    private Integer stackCount;

    @TableField("origin_x")
    private Integer originX;

    @TableField("origin_y")
    private Integer originY;

    @TableField("orientation_degrees")
    private Integer orientationDegrees;

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
    public WarehouseItem()
    {
    }

}
