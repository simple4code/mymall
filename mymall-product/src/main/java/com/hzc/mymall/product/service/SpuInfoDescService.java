package com.hzc.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 13:26:13
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

