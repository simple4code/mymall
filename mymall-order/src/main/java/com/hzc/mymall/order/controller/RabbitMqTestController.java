package com.hzc.mymall.order.controller;

import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 13:03
 */
@RestController
@Slf4j
public class RabbitMqTestController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num", defaultValue = "10") Integer num) {
        for (int i=0;i<10;i++) {
            if(i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setName("测试发送消息：" + i);
                rabbitTemplate.convertAndSend("hello-java-exchange",
                        "hello.java",
                        reasonEntity,
                        new CorrelationData(UUID.randomUUID().toString()));
                // log.info("消息【{}】发送完成", reasonEntity);
            }else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn("测试发送消息：" + UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange",
                        "hello.java",
                        orderEntity,
                        new CorrelationData(UUID.randomUUID().toString()));
                // log.info("消息【{}】发送完成", orderEntity);
            }
        }
        return "消息处理完成";
    }
}
