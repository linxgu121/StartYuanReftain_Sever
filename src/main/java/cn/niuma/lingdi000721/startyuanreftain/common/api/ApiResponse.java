package cn.niuma.lingdi000721.startyuanreftain.common.api;

import cn.niuma.lingdi000721.startyuanreftain.common.error.CommonErrorCode;
import cn.niuma.lingdi000721.startyuanreftain.common.error.ErrorCode;

import java.time.Instant;
import java.util.Objects;

/**
 * 统一响应record
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        long timestamp) {
    /**
     * record 的紧凑构造语法，在生成的主构造函数里插入校验
     */
    public ApiResponse
    {
        Objects.requireNonNull(code,"code 不能为空");
        Objects.requireNonNull(message,"message=不能为空");

        if (timestamp <= 0)
        {
            throw new IllegalArgumentException("timestamp 必须大于 0");
        }
    }

    /**
     * 成功响应，携带业务数据
     */
    public static <T> ApiResponse<T> ok(T data)
    {
        return new ApiResponse<>(
                true,
                CommonErrorCode.OK.code(),
                CommonErrorCode.OK.defaultMessage(),
                data,
                Instant.now().toEpochMilli());
    }

    /**
     * 成功响应，无返回数据
     */
    public static ApiResponse<Void> ok()
    {
        return ok(null);
    }

    /**
     * 失败响应，支持自定义提示信息
     */
    public static <T> ApiResponse<T> fail(
            ErrorCode errorCode,
            String message
    )
    {
        Objects.requireNonNull(errorCode,"errorCode 不能为空");

        String resolvedMessage = message == null || message.isBlank()
                        ? errorCode.defaultMessage()
                        : message;

        return new ApiResponse<>(
                false,
                errorCode.code(),
                resolvedMessage,
                null,
                Instant.now().toEpochMilli());
    }
}
