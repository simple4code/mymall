package com.hzc.mymall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  添加远程调用拦截器，增强远程调用
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 22:31
 */
@Configuration
public class MyMallFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1. 使用 RequestContextHolder 拿到原始请求数据
                ServletRequestAttributes requestAttributes =
                        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = requestAttributes.getRequest();
                // 2. 同步请求头数据(cookie)到 Feign 远程调用创建的新请求
                String cookie = request.getHeader("Cookie");
                template.header("Cookie", cookie);
            }
        };
    }
}
