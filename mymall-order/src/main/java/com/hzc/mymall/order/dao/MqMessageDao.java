package com.hzc.mymall.order.dao;

import com.hzc.mymall.order.entity.MqMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:46:27
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
