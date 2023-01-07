package com.hzc.mymall.product.dao;

import com.hzc.mymall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 13:26:13
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
