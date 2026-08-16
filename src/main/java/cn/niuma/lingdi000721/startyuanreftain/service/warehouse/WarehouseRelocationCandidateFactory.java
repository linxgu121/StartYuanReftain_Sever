package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.enums.WarehouseErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.RelocateWarehouseItemCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 根据当前权威快照构造物品重定位后的候选快照。
 *
 * 本类只替换目标 Placement，不检查空间碰撞、仓库边界
 * 或物品旋转策略，这些规则继续交给
 * WarehouseSnapshotValidator 统一处理。
 */
@Component
public final class WarehouseRelocationCandidateFactory {
    public ResolvedWarehouseSnapshot createCandidate(
            ResolvedWarehouseSnapshot currentSnapshot,
            RelocateWarehouseItemCommand command)
    {
        Objects.requireNonNull(
                currentSnapshot,
                "currentSnapshot 不能为空");

        Objects.requireNonNull(
                command,
                "command 不能为空");

        List<ResolvedWarehousePlacement> candidatePlacements =
                new ArrayList<>(
                        currentSnapshot.placements().size());

        boolean targetFound = false;

        for (ResolvedWarehousePlacement placement :
                currentSnapshot.placements())
        {
            if (!placement.instanceId().equals(
                    command.instanceId()))
            {
                candidatePlacements.add(placement);
                continue;
            }

            targetFound = true;

            candidatePlacements.add(
                    new ResolvedWarehousePlacement(
                            placement.instanceId(),
                            placement.itemDefinitionId(),
                            placement.count(),
                            command.originX(),
                            command.originY(),
                            command.orientationDegrees()));
        }

        if (!targetFound)
        {
            throw new BusinessException(
                    WarehouseErrorCode.ITEM_NOT_FOUND);
        }

        return new ResolvedWarehouseSnapshot(
                currentSnapshot.header(),
                currentSnapshot.catalogVersion(),
                candidatePlacements);
    }
}
