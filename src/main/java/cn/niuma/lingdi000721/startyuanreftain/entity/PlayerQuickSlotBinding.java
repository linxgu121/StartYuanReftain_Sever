package cn.niuma.lingdi000721.startyuanreftain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 一条非空快捷栏持久化绑定。
 *
 * 数据库中没有对应记录就表示该槽位为空，
 * 因此不会持久化全零 UUID。
 */
@Getter
@TableName("player_quick_slot_binding")
public class PlayerQuickSlotBinding
{
    @TableField(value = "quick_slot_id", updateStrategy = FieldStrategy.NEVER)
    private Long quickSlotId;

    @TableField(value = "slot_index", updateStrategy = FieldStrategy.NEVER)
    private Integer slotIndex;

    @TableField(value = "item_instance_uuid", updateStrategy = FieldStrategy.NEVER)
    private String itemInstanceUuid;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;

    /**
     * 供 MyBatis 通过反射创建实体。
     */
    public PlayerQuickSlotBinding()
    {
    }

    /**
     * 创建一条已经由 Service 验证过的非空绑定。
     */
    public PlayerQuickSlotBinding(
            Long quickSlotId,
            Integer slotIndex,
            String itemInstanceUuid)
    {
        this.quickSlotId = quickSlotId;
        this.slotIndex = slotIndex;
        this.itemInstanceUuid = itemInstanceUuid;
    }
}
