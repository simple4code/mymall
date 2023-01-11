package com.hzc.mymall.order.vo;

import com.hzc.mymall.order.entity.OrderEntity;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 15:38
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity orderEntity;

    /**
     * 错误状态码
     * 0 成功
     *
     */
    private Integer code;
}
