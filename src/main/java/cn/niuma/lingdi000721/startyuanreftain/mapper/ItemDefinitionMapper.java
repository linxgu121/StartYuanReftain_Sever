package cn.niuma.lingdi000721.startyuanreftain.mapper;

import cn.niuma.lingdi000721.startyuanreftain.entity.ItemDefinition;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务端权威物品定义的只读查询边界。
 *
 * 物品目录由 Flyway 管理，不向运行时业务暴露通用写入操作。
 */
public interface ItemDefinitionMapper{
    /**
     * 根据稳定物品 ID 查询启用中的定义。
     *
     * 定义不存在或已停用时返回 null。
     */
    @Select("""
            SELECT
                item_definition_id AS itemDefinitionId,
                item_type AS itemType,
                stack_policy AS stackPolicy,
                max_stack_count AS maxStackCount,
                rotation_policy AS rotationPolicy,
                definition_version AS definitionVersion,
                enabled,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM item_definition
            WHERE item_definition_id = #{itemDefinitionId}
              AND enabled = 1
            LIMIT 1
            """)
    ItemDefinition selectEnabledById(
            @Param("itemDefinitionId")
            String itemDefinitionId);

    /**
     * 批量查询启用中的物品定义。
     *
     * 不存在或已停用的定义不会出现在结果中。
     * 返回结果按照稳定物品定义 ID 升序排列。
     */
    @Select("""
        <script>
        SELECT
            item_definition_id AS itemDefinitionId,
            item_type AS itemType,
            stack_policy AS stackPolicy,
            max_stack_count AS maxStackCount,
            rotation_policy AS rotationPolicy,
            definition_version AS definitionVersion,
            enabled,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM item_definition
        WHERE enabled = 1
        <choose>
            <when test="itemDefinitionIds != null
                        and !itemDefinitionIds.isEmpty()">
                AND item_definition_id IN
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
                AND 1 = 0
            </otherwise>
        </choose>
        ORDER BY item_definition_id ASC
        </script>
        """)
    List<ItemDefinition> selectEnabledByIds(
            @Param("itemDefinitionIds")
            List<String> itemDefinitionIds);
}
