package com.hzc.mymall.order.service.impl;

import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.entity.OrderReturnReasonEntity;
import com.hzc.mymall.order.service.RabbitReceiveMsgTestService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * <p>
 *  测试 Rabbit 接收消息
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 12:32
 */
@Service
@Slf4j
@RabbitListener(queues = {"hello-java-queue"})
public class RabbitReceiveMsgTestServiceImpl implements RabbitReceiveMsgTestService {

    /**
     * 第一个参数：org.springframework.amqp.core.Message
     * 第二个参数：可以指定消息内容的类型，RabbitMq会自动转换
     * 第三个参数：获取当前数据传输的通道
     *
     * Queue：可以被多个消费者监听，只要消息被消费，队列就会删除消息，消息只会被一个消费者消费
     * 场景：
     *      1)，订单服务启动多个：同一个消息，只能有一个客户端收到
     *      2)，只有一个消息完全处理完，方法运行结束，才可以接收下一个消息
     * @param message
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) throws InterruptedException, IOException {
        // 消息体
        byte[] body = message.getBody();
        // 消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
//        log.info("接收到消息...内容：{}，类型：{}，内容：{}", message, message.getClass(), content);
//        log.info("接收到消息...类型：{}，内容：{}", message.getClass(), content);
        log.info("接收到消息...内容：{}", content);
        // Thread.sleep(3000);
        log.info("消息处理完成：{}", content.getName());
        // deliveryTag 自动自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("deliveryTag：{}", deliveryTag);
        if(deliveryTag % 2 == 0) {
            // 确认消息已被消费，非批量模式（一个一个消息确认）
            channel.basicAck(deliveryTag, false);
        }else {
            channel.basicNack(deliveryTag, false, false);
            log.info("没有确认消息 => deliveryTag：{}", deliveryTag);
        }
    }

    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderEntity content,
                               Channel channel) throws InterruptedException, IOException {
        // 消息体
        byte[] body = message.getBody();
        // 消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        log.info("接收到消息...内容：{}", content);
        // Thread.sleep(3000);
        log.info("消息处理完成：{}", content.getOrderSn());
        // deliveryTag 自动自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("deliveryTag：{}", deliveryTag);
        if(deliveryTag % 2 == 0) {
            // 确认消息已被消费，非批量模式（一个一个消息确认）
            channel.basicAck(deliveryTag, false);
        }else {
            channel.basicNack(deliveryTag, false, false);
            log.info("没有确认消息 => deliveryTag：{}", deliveryTag);
        }
    }
}
