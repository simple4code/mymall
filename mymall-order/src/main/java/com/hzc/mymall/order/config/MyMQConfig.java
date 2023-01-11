package com.hzc.mymall.order.config;

import com.hzc.mymall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-10 14:04
 */
@Configuration
@Slf4j
public class MyMQConfig {

//    @RabbitListener(queues = "order.release.order.queue")
//    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
//        log.info("收到过期的订单信息，准备关闭订单：{}", orderEntity);
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order.event.exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);
        Queue queue = new Queue("order.delay.queue",
                true,
                false,
                false,
                arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        Queue queue = new Queue("order.release.order.queue",
                true,
                false,
                false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange() {
        TopicExchange topicExchange = new TopicExchange("order.event.exchange",
                true,
                false);

        return topicExchange;
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order.event.exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order.event.exchange",
                "order.release.order",
                null);
    }

    /**
     * 订单释放和库存释放直接进行绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order.event.exchange",
                "order.release.other.#",
                null);
    }
}
