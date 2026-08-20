package cn.niuma.lingdi000721.startyuanreftain.enums;

import cn.niuma.lingdi000721.startyuanreftain.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum WarehouseErrorCode implements ErrorCode
{
    /**
     * 请求发放的物品定义不存在或已经停用。
     */
    ITEM_DEFINITION_NOT_FOUND(
            "WAREHOUSE_ITEM_DEFINITION_NOT_FOUND",
            HttpStatus.NOT_FOUND,
            "不存在可发放的物品定义"),

    /**
     * 发放数量超过物品定义允许的最大堆叠数量。
     */
    INVALID_ITEM_COUNT(
            "WAREHOUSE_INVALID_ITEM_COUNT",
            HttpStatus.BAD_REQUEST,
            "物品数量不符合定义规则"),

    /**
     * 仓库剩余空间无法容纳该物品。
     */
    NO_VALID_PLACEMENT(
            "WAREHOUSE_NO_VALID_PLACEMENT",
            HttpStatus.CONFLICT,
            "仓库没有可容纳该物品的位置"),

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
