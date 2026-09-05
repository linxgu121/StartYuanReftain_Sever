package cn.niuma.lingdi000721.startyuanreftain.controller;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.security.CurrentAccountPrincipal;
import cn.niuma.lingdi000721.startyuanreftain.converter.warehouse.QuickSlotSnapshotResponseMapper;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.QuickSlotSnapshotResponse;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.SaveQuickSlotSnapshotRequest;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.QuickSlotSaveService;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedQuickSlotSnapshot;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.QuickSlotBindingCommand;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.SaveQuickSlotSnapshotCommand;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * 当前登录玩家的快捷栏持久化保存接口。
 *
 * PUT 表示客户端提交的是完整目标快照，而不是单个槽位的增量修改。
 */
@RestController
@RequestMapping("/api/v1/game/warehouse")
public final class QuickSlotSaveController
{
    private final QuickSlotSaveService quickSlotSaveService;
    private final QuickSlotSnapshotResponseMapper responseMapper;

    public QuickSlotSaveController(
            QuickSlotSaveService quickSlotSaveService,
            QuickSlotSnapshotResponseMapper responseMapper)
    {
        this.quickSlotSaveService = Objects.requireNonNull(
                quickSlotSaveService,
                "quickSlotSaveService 不能为空");

        this.responseMapper = Objects.requireNonNull(
                responseMapper,
                "responseMapper 不能为空");
    }

    @PutMapping("/quick-slots")
    public ResponseEntity<ApiResponse<QuickSlotSnapshotResponse>>
    saveQuickSlots(
            @AuthenticationPrincipal
            CurrentAccountPrincipal currentAccount,

            @Valid
            @RequestBody
            SaveQuickSlotSnapshotRequest request)
    {
        Objects.requireNonNull(
                currentAccount,
                "currentAccount 不能为空");

        Objects.requireNonNull(
                request,
                "request 不能为空");

        SaveQuickSlotSnapshotCommand command =
                createCommand(
                        currentAccount,
                        request);

        ResolvedQuickSlotSnapshot snapshot =
                quickSlotSaveService.save(command);

        QuickSlotSnapshotResponse response =
                responseMapper.toResponse(snapshot);

        return ResponseEntity.ok(
                ApiResponse.ok(response));
    }

    /**
     * 将 HTTP DTO 转换成与 Web 层无关的内部业务命令。
     */
    private static SaveQuickSlotSnapshotCommand createCommand(
            CurrentAccountPrincipal currentAccount,
            SaveQuickSlotSnapshotRequest request)
    {
        List<QuickSlotBindingCommand> bindings =
                request.bindings()
                        .stream()
                        .map(binding ->
                                new QuickSlotBindingCommand(
                                        binding.slotIndex(),
                                        binding.itemInstanceId()))
                        .toList();

        return new SaveQuickSlotSnapshotCommand(
                currentAccount.accountUuid(),
                request.schemaVersion(),
                request.expectedRevision(),
                bindings);
    }
}