package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 已通过自身结构校验的仓库只读快照。
 *
 * 这里只检查快照集合自身能够判断的规则。
 * 物品定义、数量上限、旋转策略、边界和空间碰撞，
 * 后续由 WarehouseSnapshotService 组装时统一验证。
 */
public record ResolvedWarehouseSnapshot(
        ResolvedWarehouseHeader header,
        int catalogVersion,
        List<ResolvedWarehousePlacement> placements)
{
    public ResolvedWarehouseSnapshot
    {
        Objects.requireNonNull(header, "header 不能为空");

        Objects.requireNonNull(placements, "placements 不能为空");

        if (catalogVersion < 1)
        {
            throw new IllegalArgumentException("catalogVersion 必须大于等于 1");
        }

        List<ResolvedWarehousePlacement> copiedPlacements =
                new ArrayList<>(placements.size());

        Set<UUID> instanceIds = new HashSet<>();

        for (ResolvedWarehousePlacement placement : placements)
        {
            if (placement == null)
            {
                throw new IllegalArgumentException("placements 不能包含 null");
            }

            if (!instanceIds.add(placement.instanceId()))
            {
                throw new IllegalArgumentException(
                        "placements 不能包含重复的 instanceId："
                                + placement.instanceId());
            }

            copiedPlacements.add(placement);
        }

        placements = List.copyOf(copiedPlacements);
    }
}
