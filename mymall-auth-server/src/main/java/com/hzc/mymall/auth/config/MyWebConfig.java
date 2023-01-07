package com.hzc.mymall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-04 23:52
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    /**
     * 添加视图控制器 ViewController，让视图与url自动对应，不用再自己写controller进行映射
     * 注意这里支持的是Get请求
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
