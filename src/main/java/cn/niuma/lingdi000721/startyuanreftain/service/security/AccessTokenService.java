package cn.niuma.lingdi000721.startyuanreftain.service.security;

import cn.niuma.lingdi000721.startyuanreftain.common.security.NiumaJwtClaims;
import cn.niuma.lingdi000721.startyuanreftain.config.security.JwtProperties;
import cn.niuma.lingdi000721.startyuanreftain.service.account.AuthenticatedAccount;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *  JWT Token 的签发工厂
 *  负责把登录成功的账号信息，按照标准 JWT 规范打包成带签名的字符串
 *  返回给 UE 客户端
 */
@Service
public final class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    public AccessTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties properties,
            Clock clock) {

        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedAccessToken issue(
            AuthenticatedAccount account) {

        Objects.requireNonNull(account, "认证账号不能为空");

        Instant issuedAt = clock.instant()
                .truncatedTo(ChronoUnit.SECONDS);

        Instant expiresAt = issuedAt.plus(
                properties.getAccessTokenTtl());

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getIssuer())
                .audience(List.of(properties.getAudience()))
                .subject(account.accountUuid().toString())
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim(
                        NiumaJwtClaims.PLAYER_UID,
                        account.playerUid().toString())
                .build();

        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims));

        return new IssuedAccessToken(
                jwt.getTokenValue(),
                issuedAt,
                expiresAt);
    }

}
