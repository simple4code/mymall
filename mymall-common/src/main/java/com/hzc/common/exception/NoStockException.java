package com.hzc.common.exception;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 20:56
 */
public class NoStockException extends RuntimeException{

    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id" + skuId + "：没有足够的库存");
    }

    public NoStockException(String msg) {
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
