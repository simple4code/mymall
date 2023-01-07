package com.hzc.mymall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.coupon.entity.HomeSubjectEntity;

import java.util.Map;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:28:36
 */
public interface HomeSubjectService extends IService<HomeSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

