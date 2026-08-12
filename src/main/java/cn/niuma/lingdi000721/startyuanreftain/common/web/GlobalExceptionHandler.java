package cn.niuma.lingdi000721.startyuanreftain.common.web;

import cn.niuma.lingdi000721.startyuanreftain.common.api.ApiResponse;
import cn.niuma.lingdi000721.startyuanreftain.common.error.BusinessException;
import cn.niuma.lingdi000721.startyuanreftain.common.error.CommonErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.common.error.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 *  Spring Boot 的全局异常拦截器
 *  作用是把后端所有未捕获的异常统一翻译成之前定义的 ApiResponse 格式
 *  让 UE 客户端收到的永远是结构一致的 JSON
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception)
    {
        return createErrorResponse(
                exception.getErrorCode(),
                exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception)
    {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(CommonErrorCode.INVALID_REQUEST.defaultMessage());

        return createErrorResponse(
                CommonErrorCode.INVALID_REQUEST,
                message);
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(Exception exception)
    {
        return createErrorResponse(
                CommonErrorCode.INVALID_REQUEST,
                CommonErrorCode.INVALID_REQUEST
                        .defaultMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnreadableRequest(
            HttpMessageNotReadableException exception)
    {
        return createErrorResponse(
                CommonErrorCode.INVALID_REQUEST,
                "请求体格式不正确");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleUnexpectedException(Exception exception)
    {
        LOGGER.error(
                "未处理的服务器异常",
                exception);

        return createErrorResponse(
                CommonErrorCode.INTERNAL_ERROR,
                CommonErrorCode.INTERNAL_ERROR
                        .defaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>>
    createErrorResponse(
            ErrorCode errorCode,
            String message)
    {
        return ResponseEntity
                .status(errorCode.httpStatus())
                .body(
                        ApiResponse.fail(
                                errorCode,
                                message));
    }
}
