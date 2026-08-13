package cn.niuma.lingdi000721.startyuanreftain.config.security;

import cn.niuma.lingdi000721.startyuanreftain.common.security.CurrentAccountPrincipal;
import cn.niuma.lingdi000721.startyuanreftain.common.security.NiumaJwtClaims;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Spring Security 过滤器链中的"身份转换器"
 * 负责把已验签的 JWT 对象转换成 Spring Security 能识别的认证令牌
 */
@Component
public final class JwtCurrentAccountConverter implements Converter<Jwt,AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt)
    {
        try
        {
            Objects.requireNonNull(jwt, "jwt 不能为空");

            CurrentAccountPrincipal principal =
                    new CurrentAccountPrincipal(
                            UUID.fromString(jwt.getSubject()),
                            jwt.getClaimAsString(
                                    NiumaJwtClaims.PLAYER_UID));

            return new JwtAuthenticationToken(
                    jwt,
                    principal,
                    AuthorityUtils.NO_AUTHORITIES);
        }
        catch (RuntimeException exception)
        {
            throw new InvalidBearerTokenException(
                    "JWT身份声明无效",
                    exception);
        }
    }
}
