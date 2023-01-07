package com.hzc.mymall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-31 2:06
 */
@Data
public class PurchaseDoneVo {
    /**
     * 采购单id
     */
    private Long id;
    private List<PurchaseItemDoneVo> items;
}
