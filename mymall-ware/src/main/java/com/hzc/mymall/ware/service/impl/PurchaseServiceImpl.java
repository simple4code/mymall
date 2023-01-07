package com.hzc.mymall.ware.service.impl;

import com.hzc.common.constant.WareConstant;
import com.hzc.mymall.ware.entity.PurchaseDetailEntity;
import com.hzc.mymall.ware.service.PurchaseDetailService;
import com.hzc.mymall.ware.service.WareSkuService;
import com.hzc.mymall.ware.vo.MergeVo;
import com.hzc.mymall.ware.vo.PurchaseDoneVo;
import com.hzc.mymall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.ware.dao.PurchaseDao;
import com.hzc.mymall.ware.entity.PurchaseEntity;
import com.hzc.mymall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageunReceivePurchase(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0).or().eq("status", 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null) {
            // 如果采购单为空，需要新建一个采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setId(purchaseId);
        this.updateById(purchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        // 1. 确认采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
        .map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        if(!purchaseEntities.isEmpty()) {
            // 2. 改变采购单的状态
            this.updateBatchById(purchaseEntities);

            // 3. 改变采购项的状态
            purchaseEntities.forEach(item -> {
                List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
                List<PurchaseDetailEntity> updateEntities = entities.stream().map(entity -> {
                    PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                    detailEntity.setId(entity.getId());
                    detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    return detailEntity;
                }).collect(Collectors.toList());
                purchaseDetailService.updateBatchById(updateEntities);
            });
        }
    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();

        // 2. 改变采购项状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo itemDoneVo : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(itemDoneVo.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(itemDoneVo.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // 3. 将成功采购的入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(itemDoneVo.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(itemDoneVo.getItemId());
            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        // 1. 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode()
                : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}