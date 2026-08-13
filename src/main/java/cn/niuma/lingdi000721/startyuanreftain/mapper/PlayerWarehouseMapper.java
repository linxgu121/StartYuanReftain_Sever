package cn.niuma.lingdi000721.startyuanreftain.mapper;

import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerWarehouse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 玩家仓库的持久化边界。
 *
 * BaseMapper 当前只供账号注册事务创建初始仓库使用。
 * 仓库业务读取使用显式 SQL，不能由调用方自由组合查询条件。
 */
public interface PlayerWarehouseMapper extends BaseMapper<PlayerWarehouse> {
    /**
     * 根据账号公开 UUID 查询该账号唯一的玩家仓库。
     *
     * accountUuid 不存在时返回 null。
     */
    @Select("""
            SELECT
                warehouse.id,
                warehouse.warehouse_uuid AS warehouseUuid,
                warehouse.account_id AS accountId,
                warehouse.definition_id AS definitionId,
                warehouse.width,
                warehouse.height,
                warehouse.revision,
                warehouse.schema_version AS schemaVersion,
                warehouse.created_at AS createdAt,
                warehouse.updated_at AS updatedAt
            FROM player_warehouse AS warehouse
            INNER JOIN account AS account
                ON account.id = warehouse.account_id
            WHERE account.account_uuid = #{accountUuid}
            LIMIT 1
            """)
    PlayerWarehouse selectByAccountUuid(
            @Param("accountUuid")
            String accountUuid);

}
