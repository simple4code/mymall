package com.hzc.mymall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MymallThirdPartyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MymallThirdPartyApplication.class, args);
	}

}
