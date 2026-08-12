package cn.niuma.lingdi000721.startyuanreftain.common.error;

import java.util.Objects;

/**
 *  业务异常
 */
public class BusinessException extends RuntimeException{
    private final ErrorCode errorCode;


    public BusinessException(ErrorCode errorCode)
    {
        this(errorCode, Objects.requireNonNull(errorCode, "errorCode 不能为空").defaultMessage());
    }

    /**
     * 自定义异常提示信息
     * @param errorCode 错误码枚举
     * @param message 自定义消息，为空则使用errorCode默认message
     */
    public BusinessException(ErrorCode errorCode, String message)
    {
        super(message);

        this.errorCode = Objects.requireNonNull(errorCode, "errorCode 不能为空");
    }

    public ErrorCode getErrorCode()
    {
        return errorCode;
    }
}
