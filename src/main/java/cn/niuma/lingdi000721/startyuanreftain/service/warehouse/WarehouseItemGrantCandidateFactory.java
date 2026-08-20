package cn.niuma.lingdi000721.startyuanreftain.service.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.enums.ItemRotationPolicy;
import cn.niuma.lingdi000721.startyuanreftain.enums.WarehouseErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.service.item.ResolvedItemDefinition;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 为新发放的物品寻找第一个合法 Placement。
 *
 * 搜索顺序与 UE 保持一致：
 * 先按照 Y、X 寻找较早的逻辑格，
 * 再尝试该格子允许使用的朝向。
 *
 * 本类只构造和验证候选快照，不修改数据库。
 */
@Component
public final class WarehouseItemGrantCandidateFactory
{
    /**
     * 只允许角度=0，不允许旋转
     */
    private static final List<Integer> FIXED_ORIENTATIONS = List.of(0);

    /**
     * 允许的旋转角度（0,90,180,270）
     */
    private static final List<Integer> QUARTER_TURN_ORIENTATIONS = List.of(0, 90, 180, 270);

    private final WarehouseSnapshotValidator snapshotValidator;

    public WarehouseItemGrantCandidateFactory(
            WarehouseSnapshotValidator snapshotValidator)
    {
        this.snapshotValidator = Objects.requireNonNull(
                snapshotValidator,
                "snapshotValidator 不能为空");
    }

    /**
     * 返回包含新物品的第一个合法候选快照。
     *
     * 找不到位置时抛出稳定的业务异常；
     * 原快照始终保持不变。
     */
     public ResolvedWarehouseSnapshot createFirstValidCandidate(
             ResolvedWarehouseSnapshot currentSnapshot,
             ResolvedItemDefinition definition,
             Map<String, ResolvedItemDefinition> definitionsById,
             UUID instanceId,
             int count)
     {
         Objects.requireNonNull(currentSnapshot,"currentSnapshot 不能为空");

         Objects.requireNonNull(definition, "definition 不能为空");

         Objects.requireNonNull(definitionsById, "definitionsById 不能为空");

         Objects.requireNonNull(instanceId, "instanceId 不能为空");

         if (count <= 0)
         {
             throw new IllegalArgumentException("count 必须大于 0");
         }

         if (count > definition.maxStackCount())
         {
             throw new IllegalArgumentException("count 不能超过物品最大堆叠数量");
         }

         /*
          * 先确认原快照自身有效。
          * 原快照损坏属于服务端状态错误，
          * 不能伪装成“没有可放置位置”。
          */
         snapshotValidator.validate(currentSnapshot, definitionsById);

         //配表全量物品定义的缓存 Map,重新查一遍拿到完整的物品定义对象
         ResolvedItemDefinition mappedDefinition = definitionsById.get(definition.itemDefinitionId());

         if (!definition.equals(mappedDefinition))
         {
             throw new IllegalArgumentException("物品定义没有正确加入定义集合");
         }

         List<Integer> orientations =
                 definition.rotationPolicy()
                         == ItemRotationPolicy.FIXED
                         ? FIXED_ORIENTATIONS
                         : QUARTER_TURN_ORIENTATIONS;

         ResolvedWarehouseHeader header = currentSnapshot.header();

         /*
          *  先扫描逻辑格，再扫描该逻辑格的朝向。
          *  因此较早格子的旋转放置优先于较晚格子的零度放置。
          */
         for(int originY = 0; originY < header.height(); originY++)
         {
             for (int originX = 0; originX < header.width(); originX++)
             {
                 for (int orientationDegrees : orientations)
                 {
                     ResolvedWarehouseSnapshot candidate =
                             createCandidate(
                                     currentSnapshot,
                                     definition,
                                     instanceId,
                                     count,
                                     originX,
                                     originY,
                                     orientationDegrees);

                     if (isValidCandidate(candidate, definitionsById))
                     {
                         return candidate;
                     }
                 }
             }
         }
         throw new BusinessException(WarehouseErrorCode.NO_VALID_PLACEMENT);
     }

    /**
     * 创建包含新 Placement 的独立候选快照。
     */
    private ResolvedWarehouseSnapshot createCandidate(
            ResolvedWarehouseSnapshot currentSnapshot,
            ResolvedItemDefinition definition,
            UUID instanceId,
            int count,
            int originX,
            int originY,
            int orientationDegrees)
    {
        List<ResolvedWarehousePlacement> placements =
                new ArrayList<>(currentSnapshot.placements());

        placements.add(
                new ResolvedWarehousePlacement(
                        instanceId,
                        definition.itemDefinitionId(),
                        count,
                        originX,
                        originY,
                        orientationDegrees));

        return new ResolvedWarehouseSnapshot(
                currentSnapshot.header(),
                currentSnapshot.catalogVersion(),
                placements);
    }

    /**
     * 空间冲突、越界或旋转不合法表示当前候选不可用。
     *
     * 这里只捕获 Validator 定义的候选布局失败；
     * 原快照校验已经在搜索开始前单独执行。
     */
    private boolean isValidCandidate(
            ResolvedWarehouseSnapshot candidate,
            Map<String, ResolvedItemDefinition> definitionsById)
    {
        try
        {
            snapshotValidator.validate(
                    candidate,
                    definitionsById);

            return true;
        }
        catch (IllegalStateException exception)
        {
            return false;
        }
    }

}

