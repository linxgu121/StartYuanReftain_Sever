package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import java.util.Objects;
import java.util.UUID;

/**
 * 已从持久化行转换并通过领域校验的仓库头信息。
 *
 * 这不是 HTTP 响应 DTO，persistenceId
 * 只能用于服务端继续查询仓库物品。
 */
public record ResolvedWarehouseHeader(
        long persistenceId,
        UUID containerId,
        String definitionId,
        int width,
        int height,
        long revision,
        int schemaVersion
)
{
    private static final int MAX_GRID_DIMENSION = 200;

    public ResolvedWarehouseHeader
    {
        Objects.requireNonNull(
                containerId,
                "containerId 不能为空");

        Objects.requireNonNull(
                definitionId,
                "definitionId 不能为空");

        if (persistenceId <= 0)
        {
            throw new IllegalArgumentException(
                    "persistenceId 必须大于 0");
        }

        if (definitionId.isBlank())
        {
            throw new IllegalArgumentException(
                    "definitionId 不能为空");
        }

        if (width < 1 || width > MAX_GRID_DIMENSION)
        {
            throw new IllegalArgumentException(
                    "width 必须在 1 到 200 之间");
        }

        if (height < 1 || height > MAX_GRID_DIMENSION)
        {
            throw new IllegalArgumentException(
                    "height 必须在 1 到 200 之间");
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
    }
}
