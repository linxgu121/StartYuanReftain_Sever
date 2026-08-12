package cn.niuma.lingdi000721.startyuanreftain.controller;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.dto.account.RegisterAccountRequest;
import cn.niuma.lingdi000721.startyuanreftain.dto.account.RegisterAccountResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.account.AccountRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 账号注册 HTTP 入口
 * Controller 只负责协议转换和请求校验
 * 不编码密码、不访问 Mapper、不管理事务
 */
@RestController
@RequestMapping("/api/v1/auth")
public final class AccountRegistrationController {
    private final AccountRegistrationService registrationService;

    public AccountRegistrationController(AccountRegistrationService registrationService)
    {
        this.registrationService = Objects.requireNonNull(registrationService,"registrationService 不能为空");
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterAccountResponse>> register(
            @Valid
            @RequestBody
            RegisterAccountRequest request)
    {
        RegisterAccountResponse response = registrationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }


}
