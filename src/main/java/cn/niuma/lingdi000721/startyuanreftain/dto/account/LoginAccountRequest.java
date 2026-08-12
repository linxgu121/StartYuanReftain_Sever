package cn.niuma.lingdi000721.startyuanreftain.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 登录请求。
 *
 * 登录校验不重复执行注册时的密码强度要求，
 * 否则将来修改密码策略后，旧密码可能无法登录。
 */
public record LoginAccountRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 32, message = "用户名长度必须在 3 到 32 个字符之间")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含英文字母、数字和下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(max = 64, message = "密码长度不能超过 64 个字符")
        @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "密码只能包含可见 ASCII 字符")
        String password)
{
    public LoginAccountRequest
    {
        if (username != null)
        {
            username = username.trim();
        }
    }
}
