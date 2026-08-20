package cn.niuma.lingdi000721.startyuanreftain.controller;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.security.CurrentAccountPrincipal;
import cn.niuma.lingdi000721.startyuanreftain.converter.warehouse.WarehouseSnapshotResponseMapper;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.GrantWarehouseItemRequest;
import cn.niuma.lingdi000721.startyuanreftain.dto.warehouse.WarehouseSnapshotResponse;
import cn.niuma.lingdi000721.startyuanreftain.mapper.WarehouseItemMapper;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.ResolvedWarehouseSnapshot;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.WarehouseItemGrantService;
import cn.niuma.lingdi000721.startyuanreftain.service.warehouse.command.GrantWarehouseItemCommand;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 开发环境专用的仓库物品发放接口。
 *
 * 该 Controller 默认不会创建。
 * 只有显式启用 niuma.dev.item-grant-enabled=true 时，
 * Spring 才会把它注册到应用中。
 *
 * 该接口只能用于开发测试，不能代替正式奖励系统。
 */
@RestController
@RequestMapping("/api/v1/dev/warehouse")
@ConditionalOnProperty(
        prefix = "niuma.dev",
        name = "item-grant-enabled",
        havingValue = "true",
        matchIfMissing = false)
public final class WarehouseItemGrantController
{
    private final WarehouseItemGrantService grantService;
    private final WarehouseSnapshotResponseMapper responseMapper;

    public WarehouseItemGrantController(
            WarehouseItemGrantService grantService,
            WarehouseSnapshotResponseMapper responseMapper)
    {
        this.grantService = Objects.requireNonNull(grantService, "grantService 不能为空");

        this.responseMapper = Objects.requireNonNull(responseMapper, "responseMapper 不能为空");
    }

    /**
     * 向当前登录玩家的仓库发放测试物品。
     *
     * 账号身份来自 JWT Principal，
     * 请求体不能指定其他玩家的 accountUuid。
     */
    @PostMapping("/items/grant")
    public ResponseEntity<ApiResponse<WarehouseSnapshotResponse>> grantWarehouseItem(
            @AuthenticationPrincipal
            CurrentAccountPrincipal currentAccount,

            @Valid
            @RequestBody
            GrantWarehouseItemRequest request)
    {
        Objects.requireNonNull(currentAccount,"currentAccount 不能为空");

        Objects.requireNonNull(request,"request不能为空");

        GrantWarehouseItemCommand command = new GrantWarehouseItemCommand(
                currentAccount.accountUuid(),
                request.itemDefinitionId(),
                request.count());

        ResolvedWarehouseSnapshot snapshot = grantService.grant(command);

        WarehouseSnapshotResponse response = responseMapper.toResponse(snapshot);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
