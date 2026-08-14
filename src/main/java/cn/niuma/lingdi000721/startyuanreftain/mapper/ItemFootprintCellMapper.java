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

    /**
     * 批量读取多个物品定义的 Footprint。
     *
     * 先按照物品定义 ID 排序；
     * 同一定义内部按照与 UE 相同的“先 Y、后 X”顺序排列。
     */
    @Select("""
        <script>
        SELECT
            item_definition_id AS itemDefinitionId,
            local_x AS localX,
            local_y AS localY
        FROM item_footprint_cell
        <choose>
            <when test="itemDefinitionIds != null
                        and !itemDefinitionIds.isEmpty()">
                WHERE item_definition_id IN
                <foreach
                    collection="itemDefinitionIds"
                    item="itemDefinitionId"
                    open="("
                    separator=","
                    close=")">
                    #{itemDefinitionId}
                </foreach>
            </when>
            <otherwise>
                WHERE 1 = 0
            </otherwise>
        </choose>
        ORDER BY
            item_definition_id ASC,
            local_y ASC,
            local_x ASC
        </script>
        """)
    List<ItemFootprintCell> selectByDefinitionIds(
            @Param("itemDefinitionIds")
            List<String> itemDefinitionIds);
}
