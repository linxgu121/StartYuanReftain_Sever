package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.service.item.ItemCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 在同一个只读事务中组装玩家仓库快照。
 *
 * 当前阶段只负责组合仓库头、Placement 和物品目录版本。
 * 完整物品定义与空间规则将在后续阶段接入。
 */
@Service
public class WarehouseSnapshotService {
    private final WarehouseHeaderService warehouseHeaderService;
    private final WarehousePlacementService warehousePlacementService;
    private final ItemCatalogService itemCatalogService;

    public WarehouseSnapshotService(
            WarehouseHeaderService warehouseHeaderService,
            WarehousePlacementService warehousePlacementService,
            ItemCatalogService itemCatalogService)
    {
        this.warehouseHeaderService = Objects.requireNonNull(
                warehouseHeaderService,
                "warehouseHeaderService 不能为空");

        this.warehousePlacementService = Objects.requireNonNull(
                warehousePlacementService,
                "warehousePlacementService 不能为空");

        this.itemCatalogService = Objects.requireNonNull(
                itemCatalogService,
                "itemCatalogService 不能为空");
    }

    /**
     * 加载当前账号必须拥有的仓库快照。
     */
    @Transactional(readOnly = true)
    public ResolvedWarehouseSnapshot loadRequiredByAccountUuid(
            UUID accountUuid)
    {
        Objects.requireNonNull(
                accountUuid,
                "accountUuid 不能为空");

        ResolvedWarehouseHeader header =
                warehouseHeaderService
                        .loadRequiredByAccountUuid(accountUuid);

        List<ResolvedWarehousePlacement> placements =
                warehousePlacementService.loadByWarehouseId(
                        header.persistenceId());

        int catalogVersion =
                itemCatalogService.getCurrentCatalogVersion();

        return new ResolvedWarehouseSnapshot(
                header,
                catalogVersion,
                placements);
    }
}
