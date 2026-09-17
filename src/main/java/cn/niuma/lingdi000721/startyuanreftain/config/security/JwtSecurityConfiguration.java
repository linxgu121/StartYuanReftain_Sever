package cn.niuma.lingdi000721.startyuanreftain.config.security;

import cn.niuma.lingdi000721.startyuanreftain.common.security.AccessTokenUse;
import cn.niuma.lingdi000721.startyuanreftain.common.security.NiumaJwtClaims;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 *  JWT 签发与验证配置中心
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class JwtSecurityConfiguration {
    //时间基准
    @Bean
    public Clock jwtClock() {
        return Clock.systemUTC();
    }

    //密钥解码与强度校验
    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {

        final byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(properties.getSecretBase64());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT 密钥不是有效的 Base64 字符串", exception);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("HS256 JWT 密钥解码后不能少于 32 字节");
        }

        return new SecretKeySpec(keyBytes,  "HmacSHA256");
    }

    //Token 签发器
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {

        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    //Token 验证器
    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey,
            JwtProperties properties) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        OAuth2TokenValidator<Jwt> issuerAndTimeValidator =
                JwtValidators.createDefaultWithIssuer(
                        properties.getIssuer());

        OAuth2TokenValidator<Jwt> tokenProfileValidator = jwt -> {
            if (validateTokenProfile(jwt, properties)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "令牌用途或必要声明无效",
                            null));
        };

        OAuth2TokenValidator<Jwt> subjectValidator =
                new JwtClaimValidator<Object>(
                        JwtClaimNames.SUB,
                        JwtSecurityConfiguration::isCanonicalUuid);

        OAuth2TokenValidator<Jwt> playerUidValidator =
                new JwtClaimValidator<Object>(
                        NiumaJwtClaims.PLAYER_UID,
                        JwtSecurityConfiguration::isValidPlayerUid);


        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerAndTimeValidator,
                        tokenProfileValidator,
                        subjectValidator,
                        playerUidValidator));

        return decoder;
    }

    /**
     * 检查用途与受众的对应关系，以及本项目要求的必要声明。
     *
     * 签名、签发者和当前时间有效性仍由其他验证环节负责。
     */
    private static boolean validateTokenProfile(
            Jwt jwt,
            JwtProperties properties) {

        final AccessTokenUse use;

        try {
            use = AccessTokenUse.fromClaim(
                    jwt.getClaims().get(NiumaJwtClaims.TOKEN_USE));
        } catch (IllegalArgumentException exception) {
            return false;
        }

        String expectedAudience = switch (use) {
            case LAUNCHER -> properties.getLauncherAudience();
            case GAME -> properties.getGameAudience();
        };

        Object audienceClaim = jwt.getClaims().get(JwtClaimNames.AUD);

        // 本项目只签发单一受众令牌，不接受同时声明两种受众。
        if (!(audienceClaim instanceof List<?> audiences)
                || audiences.size() != 1
                || !expectedAudience.equals(audiences.get(0))) {
            return false;
        }

        // 解码后的标准时间声明应为 Instant，不能缺少有效期。
        if (!(jwt.getClaims().get(JwtClaimNames.IAT)
                instanceof Instant issuedAt)
                || !(jwt.getClaims().get(JwtClaimNames.NBF)
                instanceof Instant notBefore)
                || !(jwt.getClaims().get(JwtClaimNames.EXP)
                instanceof Instant expiresAt)) {
            return false;
        }

        if (!expiresAt.isAfter(issuedAt)
                || !expiresAt.isAfter(notBefore)) {
            return false;
        }

        // 后续票据会关联签发它的启动器令牌编号。
        if (!isCanonicalUuid(
                jwt.getClaims().get(JwtClaimNames.JTI))) {
            return false;
        }

        if (use == AccessTokenUse.GAME) {
            Object gameIdClaim =
                    jwt.getClaims().get(NiumaJwtClaims.GAME_ID);

            return gameIdClaim instanceof String gameId
                    && !gameId.isBlank()
                    && gameId.length() <= 64;
        }

        // 启动器令牌不携带游戏用途的绑定声明。
        return !jwt.getClaims().containsKey(NiumaJwtClaims.GAME_ID);
    }

    private static boolean isCanonicalUuid(Object value)
    {
        if (!(value instanceof String subject))
        {
            return false;
        }

        try
        {
            return UUID.fromString(subject)
                    .toString()
                    .equals(subject);
        }
        catch (IllegalArgumentException exception)
        {
            return false;
        }
    }

    private static boolean isValidPlayerUid(Object value)
    {
        return value instanceof String playerUid
                && playerUid.matches("[1-9]\\d{8}");
    }
}
