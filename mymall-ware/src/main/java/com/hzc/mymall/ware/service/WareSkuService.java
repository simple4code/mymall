package com.hzc.mymall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.to.mq.OrderTo;
import com.hzc.common.to.mq.StockLockedTo;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.ware.entity.WareSkuEntity;
import com.hzc.mymall.ware.vo.LockStockResult;
import com.hzc.mymall.ware.vo.SkuHasStockVo;
import com.hzc.mymall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:50:36
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo to);

    void unLockStock(OrderTo to);
}

