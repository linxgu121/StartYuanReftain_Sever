package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.entity.PlayerWarehouse;
import cn.niuma.lingdi000721.startyuanreftain.mapper.PlayerWarehouseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * 根据公开账号身份加载并校验玩家仓库头信息。
 */
@Service
public class WarehouseHeaderService {
    private final PlayerWarehouseMapper warehouseMapper;

    public WarehouseHeaderService(
            PlayerWarehouseMapper warehouseMapper)
    {
        this.warehouseMapper = Objects.requireNonNull(
                warehouseMapper,
                "warehouseMapper 不能为空");
    }

    /**
     * 加载指定账号必须拥有的玩家仓库。
     *
     * 仓库不存在或持久化数据损坏都属于服务端状态异常。
     */
    @Transactional(readOnly = true)
    public ResolvedWarehouseHeader loadRequiredByAccountUuid(
            UUID accountUuid)
    {
        Objects.requireNonNull(
                accountUuid,
                "accountUuid 不能为空");

        PlayerWarehouse warehouse =
                warehouseMapper.selectByAccountUuid(
                        accountUuid.toString());

        if (warehouse == null)
        {
            throw new IllegalStateException(
                    "当前账号缺少玩家仓库：" + accountUuid);
        }

        try
        {
            return resolveWarehouseHeader(warehouse);
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalStateException(
                    "服务端玩家仓库头数据损坏：" + accountUuid,
                    exception);
        }
    }

    private ResolvedWarehouseHeader resolveWarehouseHeader(
            PlayerWarehouse warehouse)
    {
        return new ResolvedWarehouseHeader(
                requireField(
                        warehouse.getId(),
                        "id"),
                parseContainerId(
                        warehouse.getWarehouseUuid()),
                requireField(
                        warehouse.getDefinitionId(),
                        "definitionId"),
                requireField(
                        warehouse.getWidth(),
                        "width"),
                requireField(
                        warehouse.getHeight(),
                        "height"),
                requireField(
                        warehouse.getRevision(),
                        "revision"),
                requireField(
                        warehouse.getSchemaVersion(),
                        "schemaVersion"));
    }

    private static UUID parseContainerId(String warehouseUuid)
    {
        String uuidText = requireField(
                warehouseUuid,
                "warehouseUuid");

        try
        {
            UUID containerId = UUID.fromString(uuidText);

            /*
             * UUID.fromString 在部分形式下可能接受省略前导零的字符串。
             * 通过规范字符串反向比较，拒绝非标准 UUID。
             */
            if (!containerId
                    .toString()
                    .equalsIgnoreCase(uuidText))
            {
                throw new IllegalArgumentException(
                        "warehouseUuid 不是规范 UUID");
            }

            return containerId;
        }
        catch (IllegalArgumentException exception)
        {
            throw new IllegalArgumentException(
                    "warehouseUuid 不是有效的规范 UUID",
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
