package com.hzc.mymall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-31 1:14
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
