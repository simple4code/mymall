package com.hzc.mymall.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

/**
 * <p>
 *  Redis 测试
 * </p>
 *
 * @author hzc
 * @since 2023-01-03 0:33
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedisTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        // 保存
        ops.set("name", "jack" + UUID.randomUUID().toString());
        // 查询
        String name = ops.get("name");
        log.debug("之前保存的键值是：{}", name);
    }
}
