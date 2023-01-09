package com.hzc.mymall.order.vo;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 0:43
 */
@Data
public class SkuStockVo {
    private Long skuId;
    private Boolean hasStock;
}
