package cn.niuma.lingdi000721.startyuanreftain.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 的密码加密配置，用 BCrypt 算法对用户密码进行单向哈希
 */
@Configuration(proxyBeanMethods = false)
public class PasswordEncoderConfiguration {
    //Cost Factor（成本因子）
    private static final int BCRYPT_STRENGTH = 12;

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder(
                //BCrypt 算法版本标识BCryptVersion.$2B
                BCryptPasswordEncoder.BCryptVersion.$2B,
                BCRYPT_STRENGTH);
    }
}
