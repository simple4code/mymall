package com.hzc.mymall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * <p>
 *  网关统一配置跨域
 * </p>
 *
 * @author hzc
 * @since 2022-12-28 23:31
 */
@Configuration
public class MallCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration configuration = new CorsConfiguration();
        // 1.配置跨域
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedOrigin("*");
        // 是否携带 cookie 跨域
        configuration.setAllowCredentials(true);
        // 允许所有请求跨域
        source.registerCorsConfiguration("/**", configuration);

        return new CorsWebFilter(source);
    }
}
