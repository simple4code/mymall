package com.hzc.mymall.order.config;

import com.hzc.mymall.order.interceptor.LonginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 17:45
 */
@Configuration
public class OrderWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LonginUserInterceptor()).addPathPatterns("/**");
    }
}
