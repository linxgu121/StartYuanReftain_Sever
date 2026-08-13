package cn.niuma.lingdi000721.startyuanreftain.mapper;


import cn.niuma.lingdi000721.startyuanreftain.entity.ItemFootprintCell;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物品 Footprint 坐标集合的只读查询边界
 */
public interface ItemFootprintCellMapper {
    /**
     * 按照与 UE 相同的“先 Y、后 X”规范顺序读取坐标。
     */
    @Select("""
            SELECT
                item_definition_id AS itemDefinitionId,
                local_x AS localX,
                local_y AS localY
            FROM item_footprint_cell
            WHERE item_definition_id = #{itemDefinitionId}
            ORDER BY local_y ASC, local_x ASC
            """)
    List<ItemFootprintCell> selectByDefinitionId(
            @Param("itemDefinitionId")
            String itemDefinitionId);
}
