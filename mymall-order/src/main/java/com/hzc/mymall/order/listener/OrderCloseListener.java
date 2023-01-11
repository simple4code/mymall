package com.hzc.mymall.order.listener;

import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-10 20:32
 */
@Component
@RabbitListener(queues = {"order.release.order.queue"})
@Slf4j
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        log.info("收到过期的订单信息，准备关闭订单：{}", orderEntity);
        try {
            orderService.closeOrder(orderEntity);
            // todo: 手动调用支付宝收单功能

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
