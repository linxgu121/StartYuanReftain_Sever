package cn.niuma.lingdi000721.startyuanreftain.entity;

import cn.niuma.lingdi000721.startyuanreftain.enums.AccountStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@TableName(value = "account", autoResultMap = true)
public class Account {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //账号 UUID 和用户名一旦创建，终身不可更改->updateStrategy = FieldStrategy.NEVER
    @TableField(value = "account_uuid", updateStrategy = FieldStrategy.NEVER)
    private String accountUuid;

    @TableField(value = "player_uid", updateStrategy = FieldStrategy.NEVER)
    private Long playerUid;

    @TableField(value = "username", updateStrategy = FieldStrategy.NEVER)
    private String username;

    @Setter
    @TableField("password_hash")
    private String passwordHash;

    @Setter
    @TableField("status")
    private AccountStatus status;

    @TableField(value = "created_at",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;

    public Account()
    {

    }

    /**
     * 创建一个尚未写入数据库的新账号。
     */
    public Account(
            String accountUuid,
            long playerUid,
            String username,
            String passwordHash)
    {
        this.accountUuid = accountUuid;
        this.playerUid = playerUid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = AccountStatus.ACTIVE;
    }
}
