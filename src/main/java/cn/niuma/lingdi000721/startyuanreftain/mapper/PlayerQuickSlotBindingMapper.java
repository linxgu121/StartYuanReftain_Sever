package cn.niuma.lingdi000721.startyuanreftain.mapper;


import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerQuickSlotBinding;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 快捷栏非空绑定的受控持久化边界。
 *
 * 完整替换需要由 Service 在同一事务中依次完成：
 * Revision CAS、删除旧绑定、批量插入新绑定。
 */
public interface PlayerQuickSlotBindingMapper
{
    /**
     * 按槽位升序读取全部非空绑定。
     */
    @Select("""
            SELECT
                quick_slot_id AS quickSlotId,
                slot_index AS slotIndex,
                item_instance_uuid AS itemInstanceUuid,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM player_quick_slot_binding
            WHERE quick_slot_id = #{quickSlotId}
            ORDER BY slot_index ASC
            """)
    List<PlayerQuickSlotBinding> selectByQuickSlotId(
            @Param("quickSlotId")
            long quickSlotId);

    /**
     * 统计不属于快捷栏所属账号仓库的绑定。
     *
     * 正常结果必须为 0。大于 0 表示数据库中出现了
     * 跨账号绑定或已经损坏的所有权关系。
     */
    @Select("""
            SELECT COUNT(*)
            FROM player_quick_slot_binding AS binding
            INNER JOIN player_quick_slot AS quick_slot
                ON quick_slot.id = binding.quick_slot_id
            LEFT JOIN player_warehouse AS warehouse
                ON warehouse.account_id = quick_slot.account_id
            LEFT JOIN warehouse_item AS item
                ON item.warehouse_id = warehouse.id
               AND item.instance_uuid = binding.item_instance_uuid
            WHERE binding.quick_slot_id = #{quickSlotId}
              AND item.id IS NULL
            """)
    int countBindingsOutsideOwnerWarehouse(
            @Param("quickSlotId")
            long quickSlotId);

    /**
     * 删除一份快捷栏配置的全部旧绑定。
     *
     * 删除 0 行是合法情况，表示原配置本来就是空的。
     */
    @Delete("""
            DELETE FROM player_quick_slot_binding
            WHERE quick_slot_id = #{quickSlotId}
            """)
    int deleteByQuickSlotId(
            @Param("quickSlotId")
            long quickSlotId);

    /**
     * 批量插入经过 Service 验证和规范化的非空绑定。
     *
     * bindings 不能为空列表；空配置时由 Service 跳过本方法。
     */
    @Insert("""
            <script>
            INSERT INTO player_quick_slot_binding
            (
                quick_slot_id,
                slot_index,
                item_instance_uuid
            )
            VALUES
            <foreach
                collection="bindings"
                item="binding"
                separator=",">
                (
                    #{binding.quickSlotId},
                    #{binding.slotIndex},
                    #{binding.itemInstanceUuid}
                )
            </foreach>
            </script>
            """)
    int insertAll(
            @Param("bindings")
            List<PlayerQuickSlotBinding> bindings);
}
