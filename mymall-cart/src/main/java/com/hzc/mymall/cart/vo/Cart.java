package com.hzc.mymall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  购物车
 * </p>
 *
 * @author hzc
 * @since 2023-01-07 13:15
 */
public class Cart {
    List<CartItem> items;

    /**
     * 商品数量
     */
    private Integer countNum;

    /**
     * 商品类型数量
     */
    private Integer countType;

    /**
     * 商品总价
     */
    private BigDecimal totalAmount;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        return this.items != null ? items.size() : 0;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0.00");
        // 1, 计算购物项总价
        if(items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                if(item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }

        // 2, 减去优惠总价
        amount = amount.subtract(getReduce());
        return amount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
