package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import java.util.Objects;
import java.util.UUID;

/**
 * 对外返回给 UE 的仓库物品放置数据。
 */
public record WarehousePlacementResponse(
        UUID instanceId,
        String itemDefinitionId,
        int count,
        int originX,
        int originY,
        int orientationDegrees)
{
    private static final UUID EMPTY_INSTANCE_ID = new UUID(0L, 0L);

    public WarehousePlacementResponse {
        Objects.requireNonNull(
                instanceId,
                "instanceId 不能为空");

        Objects.requireNonNull(
                itemDefinitionId,
                "itemDefinitionId 不能为空");

        if (EMPTY_INSTANCE_ID.equals(instanceId)) {
            throw new IllegalArgumentException(
                    "instanceId 不能是全零 UUID");
        }

        if (itemDefinitionId.isBlank()) {
            throw new IllegalArgumentException(
                    "itemDefinitionId 不能为空");
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "count 必须大于 0");
        }

        if (originX < 0 || originY < 0) {
            throw new IllegalArgumentException(
                    "Placement 原点不能包含负坐标");
        }

        if (!isValidOrientation(orientationDegrees)) {
            throw new IllegalArgumentException(
                    "orientationDegrees 必须是 0、90、180 或 270");
        }
    }

    private static boolean isValidOrientation(
            int orientationDegrees)
    {
        return switch (orientationDegrees)
        {
            case 0, 90, 180, 270 -> true;
            default -> false;
        };
    }
}
