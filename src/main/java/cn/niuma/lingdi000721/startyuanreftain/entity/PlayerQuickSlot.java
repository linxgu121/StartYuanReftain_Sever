package cn.niuma.lingdi000721.startyuanreftain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 一个账号的快捷栏持久化头记录。
 *
 * 这里只保存独立持久化版本和协议版本，
 * 不包含游戏期间的动态容量或当前选中槽位。
 */
@Getter
@TableName("player_quick_slot")
public class PlayerQuickSlot
{
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "account_id", updateStrategy = FieldStrategy.NEVER)
    private Long accountId;

    @TableField(value = "revision", updateStrategy = FieldStrategy.NEVER)
    private Long revision;

    @TableField(value = "schema_version", updateStrategy = FieldStrategy.NEVER)
    private Integer schemaVersion;

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
    public PlayerQuickSlot()
    {
    }

    /**
     * 为新注册账号创建初始快捷栏头。
     */
    public PlayerQuickSlot(Long accountId)
    {
        this.accountId = accountId;
        this.revision = 0L;
        this.schemaVersion = 1;
    }
}
