package cn.niuma.lingdi000721.startyuanreftain.enums;

import cn.niuma.lingdi000721.startyuanreftain.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum WarehouseErrorCode implements ErrorCode
{
    ITEM_NOT_FOUND(
            "WAREHOUSE_ITEM_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "仓库中不存在该物品"),

    PLACEMENT_CONFLICT(
            "WAREHOUSE_PLACEMENT_CONFLICT",
            HttpStatus.CONFLICT,
            "目标位置无法放置该物品"),

    REVISION_CONFLICT(
            "WAREHOUSE_REVISION_CONFLICT",
            HttpStatus.CONFLICT,
            "仓库数据已发生变化，请刷新后重试");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    WarehouseErrorCode(
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
