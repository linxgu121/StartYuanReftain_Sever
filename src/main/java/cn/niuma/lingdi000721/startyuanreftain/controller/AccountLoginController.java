package cn.niuma.lingdi000721.startyuanreftain.controller;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.dto.account.LoginAccountRequest;
import cn.niuma.lingdi000721.startyuanreftain.dto.account.LoginAccountResponse;
import cn.niuma.lingdi000721.startyuanreftain.service.account.AccountLoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 登录功能
 */
@RestController
@RequestMapping("/api/v1/auth")
public final class AccountLoginController {
    private final AccountLoginService loginService;

    public AccountLoginController(AccountLoginService loginService)
    {
        this.loginService = Objects.requireNonNull(
                loginService,
                "loginService 不能为空");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginAccountResponse>> login(
            @Valid
            @RequestBody
            LoginAccountRequest request)
    {
        LoginAccountResponse response = loginService.login(request);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
