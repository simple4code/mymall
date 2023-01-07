package com.hzc.mymall.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-03 16:25
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedissonTest {
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void redissonTest() {
        System.out.println(redissonClient);
    }
}
