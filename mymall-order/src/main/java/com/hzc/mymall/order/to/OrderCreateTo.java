package com.hzc.mymall.order.to;

import com.hzc.mymall.order.entity.OrderEntity;
import com.hzc.mymall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 16:05
 */
@Data
public class OrderCreateTo {

    private OrderEntity orderEntity;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;
}
