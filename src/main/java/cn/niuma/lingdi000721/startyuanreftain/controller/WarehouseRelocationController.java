package cn.niuma.lingdi000721.startyuanreftain.controller;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.security.CurrentAccountPrincipal;
import cn.niuma.lingdi000721.startyuanreftain.converter.warehouse.WarehouseSnapshotResponseMapper;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.RelocateWarehouseItemRequest;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.WarehouseSnapshotResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedWarehouseSnapshot;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.WarehouseRelocationService;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.RelocateWarehouseItemCommand;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 当前登录玩家的仓库物品重定位接口。
 */
@RestController
@RequestMapping("/api/v1/game/warehouse")
public final class WarehouseRelocationController
{
    private final WarehouseRelocationService relocationService;
    private final WarehouseSnapshotResponseMapper responseMapper;

    public WarehouseRelocationController(
            WarehouseRelocationService relocationService,
            WarehouseSnapshotResponseMapper responseMapper)
    {
        this.relocationService = Objects.requireNonNull(
                relocationService,
                "relocationService 不能为空");

        this.responseMapper = Objects.requireNonNull(
                responseMapper,
                "responseMapper 不能为空");
    }

    @PostMapping("/items/relocate")
    public ResponseEntity<ApiResponse<WarehouseSnapshotResponse>>
    relocateWarehouseItem(
            @AuthenticationPrincipal
            CurrentAccountPrincipal currentAccount,

            @Valid
            @RequestBody
            RelocateWarehouseItemRequest request)
    {
        Objects.requireNonNull(
                currentAccount,
                "currentAccount 不能为空");

        Objects.requireNonNull(
                request,
                "request 不能为空");

        RelocateWarehouseItemCommand command =
                new RelocateWarehouseItemCommand(
                        currentAccount.accountUuid(),
                        request.instanceId(),
                        request.originX(),
                        request.originY(),
                        request.orientationDegrees(),
                        request.expectedRevision());

        ResolvedWarehouseSnapshot snapshot =
                relocationService.relocate(command);

        WarehouseSnapshotResponse response =
                responseMapper.toResponse(snapshot);

        return ResponseEntity.ok(
                ApiResponse.ok(response));
    }
}