package cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command;


import java.util.Objects;
import java.util.UUID;

/**
 * 仓库物品重定位的内部业务命令。
 *
 * 与 HTTP Request 分离，使事务服务不依赖 Web 层 DTO。
 */
public record RelocateWarehouseItemCommand(
        UUID accountUuid,
        UUID instanceId,
        int originX,
        int originY,
        int orientationDegrees,
        long expectedRevision
)
{
    private static final UUID EMPTY_UUID =
            new UUID(0L, 0L);

    public RelocateWarehouseItemCommand
    {
        Objects.requireNonNull(
                accountUuid,
                "accountUuid 不能为空");

        Objects.requireNonNull(
                instanceId,
                "instanceId 不能为空");

        if (EMPTY_UUID.equals(accountUuid))
        {
            throw new IllegalArgumentException("accountUuid 不能是全零 UUID");
        }

        if (EMPTY_UUID.equals(instanceId))
        {
            throw new IllegalArgumentException("instanceId 不能是全零 UUID");
        }

        if (originX < 0 || originY < 0)
        {
            throw new IllegalArgumentException("目标原点不能包含负坐标");
        }

        if (!isValidOrientation(orientationDegrees))
        {
            throw new IllegalArgumentException("orientationDegrees 必须是 0、90、180 或 270");
        }

        if (expectedRevision < 0)
        {
            throw new IllegalArgumentException("expectedRevision 不能小于 0");
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
