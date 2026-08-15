package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/**
 * 仓库物品原子重定位请求。
 *
 * 一次命令同时修改物品原点和朝向，
 * 对应 UE 仓库中的 TryRelocate 操作。
 */
public record RelocateWarehouseItemRequest(
        @NotNull(message = "物品实例 ID 不能为空")
        UUID instanceId,

        @NotNull(message = "目标 X 坐标不能为空")
        @PositiveOrZero(message = "目标 X 坐标不能小于 0")
        Integer originX,

        @NotNull(message = "目标 Y 坐标不能为空")
        @PositiveOrZero(message = "目标 Y 坐标不能小于 0")
        Integer originY,

        @NotNull(message = "目标朝向不能为空")
        Integer orientationDegrees,

        @NotNull(message = "预期仓库版本不能为空")
        @PositiveOrZero(message = "预期仓库版本不能小于 0")
        Long expectedRevision)
{
    private static final UUID EMPTY_INSTANCE_ID =
            new UUID(0L, 0L);

    public RelocateWarehouseItemRequest
    {
        if (EMPTY_INSTANCE_ID.equals(instanceId))
        {
            throw new IllegalArgumentException("物品实例 ID 不能是全零 UUID");
        }

        if (orientationDegrees != null
                && !isValidOrientation(orientationDegrees))
        {
            throw new IllegalArgumentException("目标朝向必须是 0、90、180 或 270");
        }
    }

    private static boolean isValidOrientation(int orientationDegrees)
    {
        return switch (orientationDegrees)
        {
            case 0, 90, 180, 270 -> true;
            default -> false;
        };
    }
}
