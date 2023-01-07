package com.hzc.mymall.ware.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.ware.dao.PurchaseDetailDao;
import com.hzc.mymall.ware.entity.PurchaseDetailEntity;
import com.hzc.mymall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("purchase_id", key).or().eq("sku_id", key));
        }

        String status = (String) params.get("status");
        if(StringUtils.isNotEmpty(status)) {
            queryWrapper.eq("status", status);
        }

        String wareId = (String) params.get("wareId");
        if(StringUtils.isNotEmpty(key)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<PurchaseDetailEntity> entities = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));

        return entities;
    }

}