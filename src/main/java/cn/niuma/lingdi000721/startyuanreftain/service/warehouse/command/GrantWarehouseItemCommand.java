package cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command;

import java.util.Objects;
import java.util.UUID;

/**
 * 仓库物品发放的内部业务命令。
 *
 * 它与 HTTP Request DTO 分离，保证仓库事务层
 * 不依赖 Jakarta Validation 或 Web 层的数据结构。
 *
 * InstanceId、位置和朝向不在命令中，
 * 后续全部由服务端生成和计算。
 */
public record GrantWarehouseItemCommand(
        UUID accountUuid,
        String itemDefinitionId,
        int count)
{
    private static final UUID EMPTY_UUID = new UUID(0L, 0L);

    public GrantWarehouseItemCommand
    {
        /*
         * 内部命令不能完全依赖 Controller 校验，
         * 因为未来奖励或邮件服务也可能直接创建它。
         */
        Objects.requireNonNull(accountUuid, "accountUuid 不能为空");

        Objects.requireNonNull(itemDefinitionId, "itemDefinitionId 不能为空");

        if (EMPTY_UUID.equals(accountUuid))
        {
            throw new IllegalArgumentException("accountUuid 不能是全零 UUID");
        }

        itemDefinitionId = itemDefinitionId.trim();

        if (itemDefinitionId.isBlank())
        {
            throw new IllegalArgumentException("itemDefinitionId 不能为空");
        }

        if (count <= 0)
        {
            throw new IllegalArgumentException("count 必须大于 0");
        }
    }
}
