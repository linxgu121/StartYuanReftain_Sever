package cn.niuma.lingdi000721.startyuanreftain.converter.warehouse;

import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.QuickSlotBindingResponse;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.QuickSlotSnapshotResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedQuickSlotBinding;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedQuickSlotSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 将服务端快捷栏持久化快照转换为 Unity 使用的 HTTP 响应。
 *
 * 这是纯转换器，不访问数据库，也不处理游戏会话快捷栏规则。
 */
@Component
public final class QuickSlotSnapshotResponseMapper
{
    public QuickSlotSnapshotResponse toResponse(
            ResolvedQuickSlotSnapshot snapshot)
    {
        Objects.requireNonNull(
                snapshot,
                "snapshot 不能为空");

        List<QuickSlotBindingResponse> bindings =
                snapshot.bindings()
                        .stream()
                        .map(this::toBindingResponse)
                        .toList();

        return new QuickSlotSnapshotResponse(
                snapshot.schemaVersion(),
                snapshot.revision(),
                bindings);
    }

    private QuickSlotBindingResponse toBindingResponse(
            ResolvedQuickSlotBinding binding)
    {
        return new QuickSlotBindingResponse(
                binding.slotIndex(),
                binding.itemInstanceId());
    }
}