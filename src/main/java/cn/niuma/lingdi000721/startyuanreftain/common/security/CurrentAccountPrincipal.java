package cn.niuma.lingdi000721.startyuanreftain.common.security;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.util.Objects;
import java.util.UUID;

/**
 * Spring Security 的认证主体（Principal）
 * 代表"当前已登录的用户身份"
 * 它把 JWT 解析后的用户信息，转化为 Java 代码里随处可访问的当前用户对象
 */
public record CurrentAccountPrincipal(UUID accountUuid,String playerUid) implements AuthenticatedPrincipal
{
    public CurrentAccountPrincipal
    {
        Objects.requireNonNull(
                accountUuid,
                "accountUuid 不能为空");

        Objects.requireNonNull(
                playerUid,
                "playerUid 不能为空");

        if (!playerUid.matches("[1-9]\\d{8}"))
        {
            throw new IllegalArgumentException(
                    "playerUid 必须是九位十进制数字");
        }
    }

    @Override
    public String getName()
    {
        return accountUuid.toString();
    }
}

