package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 完整替换快捷栏持久化快照的请求。
 *
 * bindings 是完整目标状态，不是增量操作：
 * 空数组表示清空全部快捷栏绑定，null 是非法请求。
 */
public record SaveQuickSlotSnapshotRequest(
        @NotNull(message = "快捷栏协议版本不能为空")
        @Min(value = 1, message = "快捷栏协议版本必须是 1")
        @Max(value = 1, message = "当前服务端仅支持快捷栏协议版本 1")
        Integer schemaVersion,

        @NotNull(message = "预期快捷栏版本不能为空")
        @PositiveOrZero(message = "预期快捷栏版本不能小于 0")
        Long expectedRevision,

        @Valid
        @NotNull(message = "快捷栏绑定集合不能为空")
        @Size(
                max = 256,
                message = "快捷栏绑定数量不能超过 256")
        List<QuickSlotBindingRequest> bindings)
{
    public SaveQuickSlotSnapshotRequest
    {
        if (bindings != null)
        {
            Set<Integer> usedSlots = new HashSet<>();

            for (QuickSlotBindingRequest binding : bindings)
            {
                /*
                 * null 元素和 null slotIndex 最终也会被参数校验拒绝；
                 * 这里只负责提前识别重复槽位。
                 */
                if (binding == null || binding.slotIndex() == null)
                {
                    continue;
                }

                if (!usedSlots.add(binding.slotIndex()))
                {
                    throw new IllegalArgumentException(
                            "快捷栏保存请求包含重复槽位："
                                    + binding.slotIndex());
                }
            }

            // 防止 Controller 接收到的集合在后续处理中被外部修改。
            bindings = List.copyOf(bindings);
        }
    }
}