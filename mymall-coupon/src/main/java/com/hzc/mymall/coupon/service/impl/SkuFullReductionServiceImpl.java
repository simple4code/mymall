package com.hzc.mymall.coupon.service.impl;

import com.hzc.common.to.MemberPrice;
import com.hzc.common.to.SkuReductionTo;
import com.hzc.mymall.coupon.entity.MemberPriceEntity;
import com.hzc.mymall.coupon.entity.SkuLadderEntity;
import com.hzc.mymall.coupon.service.MemberPriceService;
import com.hzc.mymall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.coupon.dao.SkuFullReductionDao;
import com.hzc.mymall.coupon.entity.SkuFullReductionEntity;
import com.hzc.mymall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 1. 保存打折 sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        // 2. 保存满减 sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, reductionEntity);
        if(reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            this.save(reductionEntity);
        }

        // 3. 保存会员价 sms_member_price
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrices.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(item.getId());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(item -> item.getMemberPrice().compareTo(new BigDecimal("0")) > 0)
        .collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }

}