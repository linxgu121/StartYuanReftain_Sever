package cn.niuma.lingdi000721.startyuanreftain.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 的过滤器链配置
 * 作用是把游戏后端默认锁死——除了健康检查接口，其他所有请求一律拒绝
 */
//声明这是一个配置类proxyBeanMethods = false：禁用 CGLIB 代理，提升启动速度。因为这里只定义了一个proxyBeanMethods = false：禁用 CGLIB 代理，提升启动速度。因为这里只定义了一个
@Configuration(proxyBeanMethods = false)
public class WebSecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {

        http
                //关闭CSRF（前后端分离API项目常用；传统表单不要关
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**")
                        .permitAll()
                        .anyRequest()
                        .denyAll());

        return http.build();
    }
}
