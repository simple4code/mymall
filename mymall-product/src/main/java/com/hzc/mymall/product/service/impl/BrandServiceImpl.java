package com.hzc.mymall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hzc.mymall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.product.dao.BrandDao;
import com.hzc.mymall.product.entity.BrandEntity;
import com.hzc.mymall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 1. 获取 key
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<BrandEntity>();
        if(StringUtils.isNotEmpty(key)) {
            queryWrapper.eq("brand_id", key).or().like("name", key);
        }

        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 更新时注意要更新 pms_category_brand_relation 表中的冗余字段
     * @param brand
     */
    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        // 保证冗余字段的数据一致
        this.updateById(brand);
        if(StringUtils.isNotEmpty(brand.getName())) {
            // 同步更新其他表中的品牌名称
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());

            // todo 更新其他表
        }
    }

}