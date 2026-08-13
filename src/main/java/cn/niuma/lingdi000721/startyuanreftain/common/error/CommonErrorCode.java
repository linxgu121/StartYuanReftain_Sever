package cn.niuma.lingdi000721.startyuanreftain.common.error;

import org.springframework.http.HttpStatus;

/**
 * 统一错误码枚举
 */
public enum CommonErrorCode implements ErrorCode {
    /*
     *HttpStatus.OK              200 成功
     *HttpStatus.BAD_REQUEST     400 参数错误
     *HttpStatus.UNAUTHORIZED    401 未登录
     *HttpStatus.FORBIDDEN       403 权限不足
     *HttpStatus.NOT_FOUND       404
     *HttpStatus.INTERNAL_SERVER_ERROR 500
     */
    OK("OK", HttpStatus.OK,""),

    INVALID_REQUEST(
            "COMMON_INVALID_REQUEST",
            HttpStatus.BAD_REQUEST,
            "请求参数不合法"),

    INTERNAL_ERROR(
            "COMMON_INTERNAL_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "服务器内部错误"),

    AUTH_UNAUTHORIZED(
        "AUTH_UNAUTHORIZED",
        HttpStatus.UNAUTHORIZED,
        "身份凭证缺失或无效");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    CommonErrorCode(
            String code,
            HttpStatus httpStatus,
            String defaultMessage)
    {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }
}
