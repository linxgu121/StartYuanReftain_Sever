package cn.niuma.lingdi000721.startyuanreftain.config.security;

import cn.niuma.lingdi000721.startyuanreftain.common.security.ApiAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
/**
 * Spring Security 的过滤器链配置
 * 作用是把游戏后端默认锁死——除了健康检查接口，其他所有请求一律拒绝
 */
//声明这是一个配置类proxyBeanMethods = false：禁用 CGLIB 代理，提升启动速度。因为这里只定义了一个proxyBeanMethods = false：禁用 CGLIB 代理，提升启动速度。因为这里只定义了一个
@Configuration(proxyBeanMethods = false)
public class WebSecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            JwtCurrentAccountConverter jwtAuthenticationConverter,
            ApiAuthenticationEntryPoint authenticationEntryPoint)
            throws Exception
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

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint))

                .oauth2ResourceServer(resourceServer ->
                        resourceServer
                                .authenticationEntryPoint(
                                        authenticationEntryPoint)
                                .jwt(jwt ->
                                        jwt.decoder(jwtDecoder)
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter)))

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login")
                        .permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/game/warehouse")
                        .authenticated()
                        .anyRequest()
                        .denyAll());

        return http.build();
    }
}
