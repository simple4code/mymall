package com.hzc.mymall.coupon.dao;

import com.hzc.mymall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:28:36
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
