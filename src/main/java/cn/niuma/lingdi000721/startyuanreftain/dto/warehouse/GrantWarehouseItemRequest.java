package cn.niuma.lingdi000721.startyuanreftain.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 开发环境测试物品发放请求。
 *
 * 客户端只能指定物品定义和数量。
 * InstanceId、位置与朝向全部由服务端生成，
 * 防止调用方绕过仓库空间规则。
 */
public record GrantWarehouseItemRequest(
        /**
         * 服务端权威物品定义 ID。
         * @NotBlank 不能为空
         * @Size 不能超过指定字符
         */
        @NotBlank(message = "物品定义 ID 不能为空")
        @Size(max = 128,message = "物品定义 ID 不能超过 128 个字符")
        String itemDefinitionId,

        /**
         * 本次发放的物品数量。
         * @NotNull 发放数量不能为空
         * @Positive 发放数量必须大于0
         */
        @NotNull(message = "发放数量不能为空")
        @Positive(message = "发放数量必须大于 0")
        Integer count)
{
    public GrantWarehouseItemRequest
    {
        /*
         * null 交给 Bean Validation 处理；
         * 这里只清除用户输入两侧的空白字符。
         */
        if (itemDefinitionId != null)
        {
            itemDefinitionId = itemDefinitionId.trim();
        }
    }
}
