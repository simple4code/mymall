package com.hzc.mymall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// 开启服务注册发现客户端
@EnableDiscoveryClient
@SpringBootApplication
public class MymallCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(MymallCouponApplication.class, args);
	}

}
