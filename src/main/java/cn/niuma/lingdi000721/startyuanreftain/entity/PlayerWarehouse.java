package cn.niuma.lingdi000721.startyuanreftain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@TableName("player_warehouse")
public class PlayerWarehouse {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "warehouse_uuid", updateStrategy = FieldStrategy.NEVER)
    private String warehouseUuid;

    @TableField(value = "account_id", updateStrategy = FieldStrategy.NEVER)
    private Long accountId;

    @TableField(value = "definition_id", updateStrategy = FieldStrategy.NEVER)
    private String definitionId;

    @TableField(value = "width", updateStrategy = FieldStrategy.NEVER)
    private Integer width;

    @TableField(value = "height", updateStrategy = FieldStrategy.NEVER)
    private Integer height;

    @TableField(value = "revision", updateStrategy = FieldStrategy.NEVER)
    private Long revision;

    @TableField(value = "schema_version", updateStrategy = FieldStrategy.NEVER)
    private Integer schemaVersion;

    @TableField(value = "created_at",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;

    public PlayerWarehouse()
    {
    }

    /**
     * 创建初始玩家仓库。
     */
    public PlayerWarehouse(
            String warehouseUuid,
            Long accountId)
    {
        this.warehouseUuid = warehouseUuid;
        this.accountId = accountId;
        this.definitionId = "PlayerWarehouse";
        this.width = 20;
        this.height = 30;
        this.revision = 0L;
        this.schemaVersion = 1;
    }
}
