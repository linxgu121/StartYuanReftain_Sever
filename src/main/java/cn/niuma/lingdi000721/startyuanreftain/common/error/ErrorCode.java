package cn.niuma.lingdi000721.startyuanreftain.common.error;

import org.springframework.http.HttpStatus;

/**
 * 错误码接口
 */
public interface ErrorCode {
    String code();

    HttpStatus httpStatus();

    String defaultMessage();
}
