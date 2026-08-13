package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import java.util.Objects;
import java.util.UUID;

/**
 * 已通过自身结构校验的仓库物品 Placement。
 *
 * 这里只验证 Placement 自己能够判断的规则。
 * 仓库边界、物品旋转策略、数量上限和空间碰撞由后续快照组装层验证。
 */
public record ResolvedWarehousePlacement(
        UUID instanceId,
        String itemDefinitionId,
        int count,
        int originX,
        int originY,
        int orientationDegrees
)
{
    private static final UUID EMPTY_INSTANCE_ID =
            new UUID(0L, 0L);

    public ResolvedWarehousePlacement
    {
        Objects.requireNonNull(
                instanceId,
                "instanceId 不能为空");

        Objects.requireNonNull(
                itemDefinitionId,
                "itemDefinitionId 不能为空");

        if (EMPTY_INSTANCE_ID.equals(instanceId))
        {
            throw new IllegalArgumentException(
                    "instanceId 不能是全零 UUID");
        }

        if (itemDefinitionId.isBlank())
        {
            throw new IllegalArgumentException(
                    "itemDefinitionId 不能为空");
        }

        if (count <= 0)
        {
            throw new IllegalArgumentException(
                    "count 必须大于 0");
        }

        if (originX < 0 || originY < 0)
        {
            throw new IllegalArgumentException(
                    "Placement 原点不能包含负坐标");
        }

        if (!isValidOrientation(orientationDegrees))
        {
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
