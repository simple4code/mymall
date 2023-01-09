package com.hzc.mymall.order;

import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 0:31
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RabbitTemplateTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        // 1. 发送消息
        // 如果发送的消息是对象，对象必须实现序列化接口，因为RabbitMq会序列化对象进行传输
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("测试发送消息");
        String msg = "Hello, world!";
//        rabbitTemplate.convertAndSend("hello-java-exchange",
//                "hello.java",
//                reasonEntity);
        log.info("消息【{}】发送完成", reasonEntity);

        // 2. 发送的消息可以是JSON类型
        // 需要配置消息转换器
        for (int i=0;i<10;i++) {
            if(i % 2 == 0) {
                reasonEntity.setName("测试发送消息：" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange",
                        "hello.java",
                        reasonEntity);
                log.info("消息【{}】发送完成", reasonEntity);
            }else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn("测试发送消息：" + UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange",
                        "hello.java",
                        orderEntity);
                log.info("消息【{}】发送完成", orderEntity);
            }
        }
    }
}
