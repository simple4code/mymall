package com.hzc.mymall.member.config;

import com.hzc.mymall.member.interceptor.LonginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-11 14:10
 */
@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LonginUserInterceptor()).addPathPatterns("/memberOrder.html");
    }
}
