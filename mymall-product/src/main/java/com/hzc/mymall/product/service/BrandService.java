package com.hzc.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 13:26:13
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

