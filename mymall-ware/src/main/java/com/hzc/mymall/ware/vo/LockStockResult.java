package com.hzc.mymall.ware.vo;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 19:49
 */
@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private Boolean locked;


}
