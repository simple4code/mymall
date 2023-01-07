package com.hzc.mymall.ware.vo;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-31 2:07
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
