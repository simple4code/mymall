package com.hzc.mymall.order.web;

import com.hzc.mymall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 15:49
 */
@Controller
public class HelloController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{page}.html")
    public String page(@PathVariable("page") String page) {
        return page;
    }

    @GetMapping("/test/createOrder")
    @ResponseBody
    public String createOrderTest() {
        // 订单下单成功
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());

        // 给 MQ 发送消息
        rabbitTemplate.convertAndSend("order.event.exchange", "order.create.order", orderEntity);
        return "ok";
    }
}
