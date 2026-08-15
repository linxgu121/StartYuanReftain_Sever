package cn.niuma.lingdi000721.startyuanreftain.mapper;

import cn.niuma.lingdi000721.startyuanreftain.entity.WarehouseItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 按仓库内部主键读取全部持久化物品。
 *
 * 使用内部自增主键排序，保证同一数据库中的读取顺序稳定。
 */
public interface WarehouseItemMapper {
    @Select("""
            SELECT
                id,
                instance_uuid AS instanceUuid,
                warehouse_id AS warehouseId,
                item_definition_id AS itemDefinitionId,
                stack_count AS stackCount,
                origin_x AS originX,
                origin_y AS originY,
                orientation_degrees AS orientationDegrees,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM warehouse_item
            WHERE warehouse_id = #{warehouseId}
            ORDER BY id ASC
            """)
    List<WarehouseItem> selectByWarehouseId(@Param("warehouseId") long warehouseId);

    /**
     * 更新指定仓库内某个物品实例的位置和朝向。
     * warehouseId 同时作为所有权边界，
     * 防止通过 instanceUuid 修改其他玩家仓库中的物品。
     * 返回 1：目标物品存在并完成更新。
     * 返回 0：该仓库中不存在这个物品实例。
     */
    @Update("""
        UPDATE warehouse_item
        SET origin_x = #{originX},
            origin_y = #{originY},
            orientation_degrees = #{orientationDegrees}
        WHERE warehouse_id = #{warehouseId}
          AND instance_uuid = #{instanceUuid}
        """)
    int updatePlacement(
            @Param("warehouseId")
            long warehouseId,

            @Param("instanceUuid")
            String instanceUuid,

            @Param("originX")
            int originX,

            @Param("originY")
            int originY,

            @Param("orientationDegrees")
            int orientationDegrees);
}
