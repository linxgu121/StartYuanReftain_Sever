package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.entity.WarehouseItem;
import cn.niuma.lingdi000721.startyuanreftain.mapper.WarehouseItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 加载并转换指定仓库中的全部 Placement。
 *
 * 这里只验证持久化行结构和 Placement 自身规则，
 * 不负责加载物品定义、检查边界或检测空间碰撞。
 */
@Service
public class WarehousePlacementService {
    private final WarehouseItemMapper warehouseItemMapper;

    public WarehousePlacementService(
            WarehouseItemMapper warehouseItemMapper)
    {
        this.warehouseItemMapper = Objects.requireNonNull(
                warehouseItemMapper,
                "warehouseItemMapper 不能为空");
    }

    /**
     * 按数据库内部仓库 ID 加载 Placement。
     *
     * 返回列表保持 Mapper 的内部行 ID 升序，并且不可修改。
     */
    @Transactional(readOnly = true)
    public List<ResolvedWarehousePlacement> loadByWarehouseId(
            long warehouseId)
    {
        if (warehouseId <= 0)
        {
            throw new IllegalArgumentException("warehouseId 必须大于 0");
        }

        List<WarehouseItem> rows =
                warehouseItemMapper.selectByWarehouseId(
                        warehouseId);

        if (rows == null)
        {
            throw new IllegalStateException(
                    "仓库物品查询结果不能为 null："
                            + warehouseId);
        }

        try
        {
            return resolveRows(
                    warehouseId,
                    rows);
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalStateException(
                    "服务端仓库 Placement 数据损坏："
                            + warehouseId,
                    exception);
        }
    }

    private List<ResolvedWarehousePlacement> resolveRows(
            long expectedWarehouseId,
            List<WarehouseItem> rows)
    {
        List<ResolvedWarehousePlacement> placements =
                new ArrayList<>(rows.size());

        long previousRowId = 0;

        for (WarehouseItem row : rows)
        {
            if (row == null)
            {
                throw new IllegalArgumentException(
                        "仓库物品查询结果不能包含 null 行");
            }

            long rowId = requireField(
                    row.getId(),
                    "id");

            if (rowId <= 0)
            {
                throw new IllegalArgumentException(
                        "warehouse_item.id 必须大于 0");
            }

            /*
             * Mapper 契约要求 ORDER BY id ASC。
             * 加载层验证顺序，防止未来 SQL 修改后悄悄改变快照顺序。
             */
            if (rowId <= previousRowId)
            {
                throw new IllegalArgumentException(
                        "仓库物品查询结果没有按 id 严格升序排列");
            }

            previousRowId = rowId;

            long actualWarehouseId = requireField(
                    row.getWarehouseId(),
                    "warehouseId");

            if (actualWarehouseId != expectedWarehouseId)
            {
                throw new IllegalArgumentException(
                        "仓库物品行不属于目标仓库");
            }

            placements.add(
                    resolvePlacement(row));
        }

        return List.copyOf(placements);
    }

    private ResolvedWarehousePlacement resolvePlacement(
            WarehouseItem row)
    {
        return new ResolvedWarehousePlacement(
                parseInstanceId(
                        row.getInstanceUuid()),
                requireField(
                        row.getItemDefinitionId(),
                        "itemDefinitionId"),
                requireField(
                        row.getStackCount(),
                        "stackCount"),
                requireField(
                        row.getOriginX(),
                        "originX"),
                requireField(
                        row.getOriginY(),
                        "originY"),
                requireField(
                        row.getOrientationDegrees(),
                        "orientationDegrees"));
    }

    private static UUID parseInstanceId(
            String instanceUuid)
    {
        String uuidText = requireField(
                instanceUuid,
                "instanceUuid");

        try
        {
            UUID instanceId =
                    UUID.fromString(uuidText);

            if (!instanceId
                    .toString()
                    .equalsIgnoreCase(uuidText))
            {
                throw new IllegalArgumentException(
                        "instanceUuid 不是规范 UUID");
            }

            return instanceId;
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalArgumentException(
                    "instanceUuid 不是有效的规范 UUID",
                    exception);
        }
    }

    private static <T> T requireField(
            T value,
            String fieldName)
    {
        if (value == null)
        {
            throw new IllegalArgumentException(
                    fieldName + " 不能为空");
        }

        return value;
    }

}
