package com.hzc.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-10 16:56
 */
@Data
public class StockLockedTo {

    /**
     * 库存工作单的 id
     */
    private Long id;

    /**
     * 工作单详情
     */
    private StockDetailTo detail;
}
