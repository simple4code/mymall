package com.hzc.common.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 *  封装订单提交的数据
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 14:52
 */
@Data
public class OrderSubmitVo {
    /**
     * 地址id
     */
    private Long addrId;
    /**
     * 支付方式
     */
    private Integer payType;
    // 无需提交购买的商品，会再去购物车查询一次
    // 优惠，发票
    /**
     * 防重令牌
     */
    private String orderToken;
    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    // 用户相关信息，直接去 session 获取登录的用户
    /**
     * 订单备注
     */
    private String note;
}
