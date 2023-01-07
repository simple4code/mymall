package com.hzc.mymall.order.dao;

import com.hzc.mymall.order.entity.RefundInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款信息
 * 
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:46:27
 */
@Mapper
public interface RefundInfoDao extends BaseMapper<RefundInfoEntity> {
	
}
