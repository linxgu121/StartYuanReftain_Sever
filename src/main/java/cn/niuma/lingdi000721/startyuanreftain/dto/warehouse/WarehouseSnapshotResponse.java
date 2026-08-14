package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 对外返回给 UE 的完整仓库快照。
 *
 * 不包含数据库内部仓库主键 persistenceId。
 */
public record WarehouseSnapshotResponse(
        UUID containerId,
        String definitionId,
        int width,
        int height,
        long revision,
        int schemaVersion,
        int catalogVersion,
        List<WarehousePlacementResponse> placements)
{
    private static final UUID EMPTY_CONTAINER_ID = new UUID(0L, 0L);

    public WarehouseSnapshotResponse
    {
        Objects.requireNonNull(
                containerId,
                "containerId 不能为空");

        Objects.requireNonNull(
                definitionId,
                "definitionId 不能为空");

        Objects.requireNonNull(
                placements,
                "placements 不能为空");

        if (EMPTY_CONTAINER_ID.equals(containerId))
        {
            throw new IllegalArgumentException(
                    "containerId 不能是全零 UUID");
        }

        if (definitionId.isBlank())
        {
            throw new IllegalArgumentException(
                    "definitionId 不能为空");
        }

        if (width <= 0 || height <= 0)
        {
            throw new IllegalArgumentException(
                    "仓库尺寸必须大于 0");
        }

        if (revision < 0)
        {
            throw new IllegalArgumentException(
                    "revision 不能小于 0");
        }

        if (schemaVersion < 1)
        {
            throw new IllegalArgumentException(
                    "schemaVersion 必须大于等于 1");
        }

        if (catalogVersion < 1)
        {
            throw new IllegalArgumentException(
                    "catalogVersion 必须大于等于 1");
        }

        /*
         * 创建不可修改的防御性副本。
         * List.copyOf 也会拒绝列表中的 null 元素。
         */
        placements = List.copyOf(placements);
    }
}
