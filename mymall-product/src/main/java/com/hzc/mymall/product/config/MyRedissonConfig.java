package com.hzc.mymall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-03 16:16
 */
@Configuration
public class MyRedissonConfig {
    /**
     * RedissonClient 支持对 Redisson 的所有操作
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.3.142:6379");
        // 根据config创建RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
