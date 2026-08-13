package cn.niuma.lingdi000721.startyuanreftain.common.security;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.error.CommonErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *  Spring Security 的认证入口点
 *  专门处理"未认证用户试图访问受保护资源"的场景
 */
@Component
public final class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper)
    {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper 不能为空");
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException
    {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name());
        response.setHeader(
                HttpHeaders.WWW_AUTHENTICATE,
                "Bearer");

        ApiResponse<Void> body = ApiResponse.fail(
                CommonErrorCode.AUTH_UNAUTHORIZED,
                null);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
