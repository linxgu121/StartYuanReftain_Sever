package cn.niuma.lingdi000721.startyuanreftain.controller;


import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.security.CurrentAccountPrincipal;
import cn.niuma.lingdi000721.startyuanreftain.converter.warehouse.WarehouseSnapshotResponseMapper;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.WarehouseSnapshotResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedWarehouseSnapshot;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.WarehouseSnapshotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 当前登录玩家的仓库只读接口。
 */
@RestController
@RequestMapping("/api/v1/game")
public final class WarehouseSnapshotController {
    private final WarehouseSnapshotService warehouseSnapshotService;
    private final WarehouseSnapshotResponseMapper responseMapper;

    public WarehouseSnapshotController(
            WarehouseSnapshotService warehouseSnapshotService,
            WarehouseSnapshotResponseMapper responseMapper)
    {
        this.warehouseSnapshotService = Objects.requireNonNull(
                warehouseSnapshotService,
                "warehouseSnapshotService 不能为空");

        this.responseMapper = Objects.requireNonNull(
                responseMapper,
                "responseMapper 不能为空");
    }

    @GetMapping("/warehouse")
    public ResponseEntity<ApiResponse<WarehouseSnapshotResponse>>
    getWarehouse(
            @AuthenticationPrincipal
            CurrentAccountPrincipal currentAccount)
    {
        Objects.requireNonNull(
                currentAccount,
                "currentAccount 不能为空");

        ResolvedWarehouseSnapshot snapshot =
                warehouseSnapshotService
                        .loadRequiredByAccountUuid(
                                currentAccount.accountUuid());

        WarehouseSnapshotResponse response =
                responseMapper.toResponse(snapshot);

        return ResponseEntity.ok(
                ApiResponse.ok(response));
    }

}
