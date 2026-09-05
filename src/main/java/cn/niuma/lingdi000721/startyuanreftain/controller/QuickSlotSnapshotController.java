package cn.niuma.lingdi000721.startyuanreftain.controller;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.security.CurrentAccountPrincipal;
import cn.niuma.lingdi000721.startyuanreftain.converter.warehouse.QuickSlotSnapshotResponseMapper;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.QuickSlotSnapshotResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.QuickSlotSnapshotService;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedQuickSlotSnapshot;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 当前登录玩家的快捷栏持久化读取接口。
 *
 * 返回的是上次保存的绑定快照，不包含游戏中的动态容量和当前选中槽位。
 */
@RestController
@RequestMapping("/api/v1/game")
public final class QuickSlotSnapshotController
{
    private final QuickSlotSnapshotService quickSlotSnapshotService;
    private final QuickSlotSnapshotResponseMapper responseMapper;

    public QuickSlotSnapshotController(
            QuickSlotSnapshotService quickSlotSnapshotService,
            QuickSlotSnapshotResponseMapper responseMapper)
    {
        this.quickSlotSnapshotService = Objects.requireNonNull(
                quickSlotSnapshotService,
                "quickSlotSnapshotService 不能为空");

        this.responseMapper = Objects.requireNonNull(
                responseMapper,
                "responseMapper 不能为空");
    }

    @GetMapping("/warehouse/quick-slots")
    public ResponseEntity<ApiResponse<QuickSlotSnapshotResponse>>
    getQuickSlots(
            @AuthenticationPrincipal
            CurrentAccountPrincipal currentAccount)
    {
        Objects.requireNonNull(
                currentAccount,
                "currentAccount 不能为空");

        ResolvedQuickSlotSnapshot snapshot =
                quickSlotSnapshotService
                        .loadRequiredByAccountUuid(
                                currentAccount.accountUuid());

        QuickSlotSnapshotResponse response =
                responseMapper.toResponse(snapshot);

        return ResponseEntity.ok(
                ApiResponse.ok(response));
    }
}