package cn.niuma.lingdi000721.startyuanreftain.mapper;

import cn.niuma.lingdi000721.startyuanreftain.entity.WarehouseItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
