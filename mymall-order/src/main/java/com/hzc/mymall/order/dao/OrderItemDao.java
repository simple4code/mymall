package com.hzc.mymall.order.dao;

import com.hzc.mymall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:46:27
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
