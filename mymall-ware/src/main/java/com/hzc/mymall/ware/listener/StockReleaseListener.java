package com.hzc.mymall.ware.listener;

import com.alibaba.fastjson.TypeReference;
import com.hzc.common.to.mq.OrderTo;
import com.hzc.common.to.mq.StockDetailTo;
import com.hzc.common.to.mq.StockLockedTo;
import com.hzc.common.utils.R;
import com.hzc.mymall.ware.dao.WareSkuDao;
import com.hzc.mymall.ware.entity.WareOrderTaskDetailEntity;
import com.hzc.mymall.ware.entity.WareOrderTaskEntity;
import com.hzc.mymall.ware.feign.OrderFeignService;
import com.hzc.mymall.ware.service.WareOrderTaskDetailService;
import com.hzc.mymall.ware.service.WareOrderTaskService;
import com.hzc.mymall.ware.service.WareSkuService;
import com.hzc.mymall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-10 19:51
 */
@Service
@RabbitListener(queues = {"stock.release.stock.queue"})
@Slf4j
public class StockReleaseListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 1, 库存自动解锁
     *      下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前锁定的库存需要自动解锁
     * 2, 订单失败
     *      锁库存失败
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("收到解锁库存消息");

        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e) {
            // 发生异常，拒绝消息并将消息重新放回队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        log.info("收到订单关闭消息，准备解锁库存");

        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e) {
            // 发生异常，拒绝消息并将消息重新放回队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
