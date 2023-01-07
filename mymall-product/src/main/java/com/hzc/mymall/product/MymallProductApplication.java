package com.hzc.mymall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 整合 mybatis plus
 * 		-- 导入依赖
 * 		-- 配置数据源
 * 		-- 配置 mp
 */
//@EnableCaching
@EnableRedisHttpSession
@EnableFeignClients(basePackages = {"com.hzc.mymall.product.feign"})
@MapperScan("com.hzc.mymall.product.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class MymallProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(MymallProductApplication.class, args);
	}

}
