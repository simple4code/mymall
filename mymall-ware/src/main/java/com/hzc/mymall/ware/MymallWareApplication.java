package com.hzc.mymall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.hzc.mymall.ware.feign"})
@SpringBootApplication
@EnableDiscoveryClient
public class MymallWareApplication {

	public static void main(String[] args) {
		SpringApplication.run(MymallWareApplication.class, args);
	}

}
