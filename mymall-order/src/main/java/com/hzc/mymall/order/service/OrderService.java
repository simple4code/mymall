package com.hzc.mymall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.vo.OrderConfirmVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:46:27
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;
}

