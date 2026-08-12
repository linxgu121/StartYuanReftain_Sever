package cn.niuma.lingdi000721.startyuanreftain.enums;

import cn.niuma.lingdi000721.startyuanreftain.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 帐号错误码
 */
public enum AccountErrorCode implements ErrorCode {
    USERNAME_ALREADY_EXISTS(
            "ACCOUNT_USERNAME_ALREADY_EXISTS",
            HttpStatus.CONFLICT,
            "用户名已被使用"),

    INVALID_CREDENTIALS(
            "ACCOUNT_INVALID_CREDENTIALS",
            HttpStatus.UNAUTHORIZED,
            "用户名或密码错误"),

    ACCOUNT_BANNED(
            "ACCOUNT_BANNED",
            HttpStatus.FORBIDDEN,
            "账号已被封禁");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    AccountErrorCode(
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
