package cn.niuma.lingdi000721.startyuanreftain.common.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求
 */
public record RegisterAccountRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 32, message = "用户名长度必须在 3 到 32 个字符之间")
        @Pattern(regexp = "^[A-Za-z0-9_]+$",message = "用户名只能包含英文字母、数字和下划线" )
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 64, message = "密码长度必须在 8 到 64 个字符之间")
        @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "密码只能包含可见 ASCII 字符")
        String password
)
{
    public RegisterAccountRequest
    {
        if (username != null)
        {
            username = username.trim();
        }
    }
}
