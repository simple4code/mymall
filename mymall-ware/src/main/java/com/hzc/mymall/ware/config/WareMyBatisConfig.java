package com.hzc.mymall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-31 14:53
 */
@EnableTransactionManagement
@Configuration
@MapperScan(basePackages = {"com.hzc.mymall.ware.dao"})
public class WareMyBatisConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        return paginationInterceptor;
    }
}
