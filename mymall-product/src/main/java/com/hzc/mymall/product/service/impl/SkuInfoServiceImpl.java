package com.hzc.mymall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hzc.mymall.product.entity.SkuImagesEntity;
import com.hzc.mymall.product.entity.SpuInfoDescEntity;
import com.hzc.mymall.product.service.*;
import com.hzc.mymall.product.vo.SkuItemSaleAttrVo;
import com.hzc.mymall.product.vo.SkuItemVo;
import com.hzc.mymall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.product.dao.SkuInfoDao;
import com.hzc.mymall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("sku_id", key).or().like("sku_name", key));
        }

        String catelogId = (String) params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }

        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !"0".equals(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if(StringUtils.isNotEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if(StringUtils.isNotEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0")) > 0) {
                    queryWrapper.le("price", max);
                }
            }catch (Exception e){}
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据 spuId 查询所有对应的 sku实体类
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    /**
     * 商品详情获取
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1, sku 基本信息获取 pms_sku_info
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3, 获取 spu 销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4, 获取 spu 的介绍 pms_spu_info_desc
            SpuInfoDescEntity spuInfo = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfo);
        }, threadPoolExecutor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5, 获取 spu 的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos =
                    attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            //2, sku 图片信息获取 pms_sku_images
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        // 等待所有任务都完成
        try {
            CompletableFuture.allOf(infoFuture, saleAttrFuture, descFuture, baseAttrFuture, imagesFuture).get();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return skuItemVo;
    }

}