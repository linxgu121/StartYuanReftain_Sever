package cn.niuma.lingdi000721.startyuanreftain.mapper;

import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerQuickSlot;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface PlayerQuickSlotMapper extends BaseMapper<PlayerQuickSlot>
{
    /**
     * 根据经过认证的账号 UUID 读取唯一快捷栏头。
     */
    @Select("""
            SELECT
                quick_slot.id,
                quick_slot.account_id AS accountId,
                quick_slot.revision,
                quick_slot.schema_version AS schemaVersion,
                quick_slot.created_at AS createdAt,
                quick_slot.updated_at AS updatedAt
            FROM player_quick_slot AS quick_slot
            INNER JOIN account AS account
                ON account.id = quick_slot.account_id
            WHERE account.account_uuid = #{accountUuid}
            LIMIT 1
            """)
    PlayerQuickSlot selectByAccountUuid(
            @Param("accountUuid")
            String accountUuid);

    /**
     * 仅当当前持久化版本与 expectedRevision 一致时，
     * 才原子地增加快捷栏版本。
     *
     * 返回 1 表示成功；
     * 返回 0 表示记录不存在或发生版本冲突。
     */
    @Update("""
            UPDATE player_quick_slot
            SET revision = revision + 1
            WHERE id = #{quickSlotId}
              AND revision = #{expectedRevision}
            """)
    int incrementRevisionIfMatches(
            @Param("quickSlotId")
            long quickSlotId,

            @Param("expectedRevision")
            long expectedRevision);
}

