package com.hzc.mymall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  订单确认需要用的数据
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 17:56
 */
public class OrderConfirmVo {

    /**
     * 订单地址
     */
    List<MemberAddressVo> address;

    /**
     * 所有选中的购物项
     */
    List<OrderItemVo> items;

    // 发票记录...

    /**
     * 优惠卷信息
     */
    Integer integration;

    /**
     * 订单总额
     */
    BigDecimal total;

    /**
     * 应付价格
     */
    BigDecimal payPrice;

    @Getter @Setter
    Map<Long, Boolean> stocks;

    /**
     * 防重令牌
     */
    @Getter @Setter
    String orderToken;

    public List<MemberAddressVo> getAddress() {
        return address;
    }

    public void setAddress(List<MemberAddressVo> address) {
        this.address = address;
    }

    public List<OrderItemVo> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVo> items) {
        this.items = items;
    }

    public Integer getIntegration() {
        return integration;
    }

    public void setIntegration(Integer integration) {
        this.integration = integration;
    }

    public BigDecimal getTotal() {
        BigDecimal total = new BigDecimal("0.00");
        if(items != null) {
            for (OrderItemVo item : items) {
                total = total.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }
        return total;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    public Integer getCount() {
        return items != null ? items.stream().map(OrderItemVo::getCount).reduce(0, Integer::sum) : 0;
    }
}
