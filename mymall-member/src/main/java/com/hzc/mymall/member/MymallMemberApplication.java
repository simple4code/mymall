package com.hzc.mymall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.hzc.mymall.member.feign"})
@EnableDiscoveryClient
@SpringBootApplication
public class MymallMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(MymallMemberApplication.class, args);
	}

}
