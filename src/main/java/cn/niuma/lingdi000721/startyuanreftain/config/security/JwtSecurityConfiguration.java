package cn.niuma.lingdi000721.startyuanreftain.config.security;

import cn.niuma.lingdi000721.startyuanreftain.common.security.NiumaJwtClaims;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
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

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        JwtClaimNames.AUD,
                        audiences ->
                                audiences != null
                                        && audiences.contains(
                                        properties.getAudience()));

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
                        audienceValidator,
                        subjectValidator,
                        playerUidValidator));

        return decoder;
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
