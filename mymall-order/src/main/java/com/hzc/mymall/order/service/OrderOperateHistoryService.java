package com.hzc.mymall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:46:27
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

