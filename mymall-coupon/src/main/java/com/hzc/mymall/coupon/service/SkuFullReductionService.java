package com.hzc.mymall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.to.SkuReductionTo;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:28:36
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

