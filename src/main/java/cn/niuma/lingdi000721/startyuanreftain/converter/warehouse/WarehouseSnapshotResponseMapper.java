package cn.niuma.lingdi000721.startyuanreftain.converter.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.WarehousePlacementResponse;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.WarehouseSnapshotResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedWarehouseHeader;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedWarehousePlacement;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedWarehouseSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 把服务端领域快照转换成对外 HTTP 响应。
 *
 * 这是纯转换器：
 * 不访问数据库、不调用 Service、不修改领域对象。
 */
@Component
public class WarehouseSnapshotResponseMapper {
    public WarehouseSnapshotResponse toResponse(
            ResolvedWarehouseSnapshot snapshot)
    {
        Objects.requireNonNull(snapshot, "snapshot 不能为空");

        ResolvedWarehouseHeader header = snapshot.header();

        List<WarehousePlacementResponse> placements =
                snapshot.placements()
                        .stream()
                        .map(this::toPlacementResponse)
                        .toList();

        return new WarehouseSnapshotResponse(
                header.containerId(),
                header.definitionId(),
                header.width(),
                header.height(),
                header.revision(),
                header.schemaVersion(),
                snapshot.catalogVersion(),
                placements);
    }

    private WarehousePlacementResponse toPlacementResponse(
            ResolvedWarehousePlacement placement)
    {
        return new WarehousePlacementResponse(
                placement.instanceId(),
                placement.itemDefinitionId(),
                placement.count(),
                placement.originX(),
                placement.originY(),
                placement.orientationDegrees());
    }
}
