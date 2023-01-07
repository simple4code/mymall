package com.hzc.mymall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hzc.common.constant.ProductConstant;
import com.hzc.common.to.SkuHasStockVo;
import com.hzc.common.to.SkuReductionTo;
import com.hzc.common.to.SpuBoundTo;
import com.hzc.common.to.es.SkuEsModel;
import com.hzc.common.utils.R;
import com.hzc.mymall.product.entity.*;
import com.hzc.mymall.product.feign.CouponFeignService;
import com.hzc.mymall.product.feign.SearchFeignService;
import com.hzc.mymall.product.feign.WareFeignService;
import com.hzc.mymall.product.service.*;
import com.hzc.mymall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1. 保存 SPU 基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2. 保存 SPU 描述图片 pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.save(descEntity);

        // 3. 保存 SPU 图片集 pms_spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 4. 保存 SPU 规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(item.getAttrId());
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            if (attrEntity != null) {
                valueEntity.setAttrName(attrEntity.getAttrName());
            }
            valueEntity.setAttrValue(item.getAttrValues());
            valueEntity.setQuickShow(item.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueEntities);

        // 5. 保存 SPU 积分信息 gulimall_sms -> sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0) {
            log.error("远程保存 SPU 积分信息失败");
        }

        // 5. 保存 SPU 对应的 SKU 信息
        // 5.1 保存 SKU 基本信息 pms_sku_info
        List<Skus> skus = spuSaveVo.getSkus();
        if(skus != null && !skus.isEmpty()) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                // 5.2 保存 SKU 图片信息 pms_sku_images
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    imagesEntity.setSkuId(skuId);
                    imagesEntity.setDefaultImg(img.getDefaultImg());
                    imagesEntity.setImgUrl(img.getImgUrl());
                    return imagesEntity;
                }).filter(image -> StringUtils.isNotEmpty(image.getImgUrl())).collect(Collectors.toList());
                // todo: 没有图片路径的无需保存
                skuImagesService.saveBatch(imagesEntities);

                // 5.3 保存 SKU 销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 5.4 保存 SKU 的优惠，满减信息
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0
                        || skuReductionTo.getDiscount().compareTo(new BigDecimal("0")) > 0) {
                    R res = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(res.getCode() != 0) {
                        log.error("远程保存 SKU 优惠满减信息失败");
                    }
                }
            });
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !"0".equals(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String status = (String) params.get("status");
        if(StringUtils.isNotEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String catelogId = (String) params.get("catelogId");
        if(StringUtils.isNotEmpty(catelogId) && !"0".equals(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品上架
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        // 1. 组装需要的数据
        // 1.1 查出当前spuid对应的所有sku信息，品牌名称
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 查询当前 sku 的所有可以用来被检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idsSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream()
                .filter(item -> idsSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());

        // 远程调用库存服务，查询是否有库存
        Map<Long, Boolean> skusHasStockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIds);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            skusHasStockMap = r.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        }catch (Exception e) {
            log.error("库存查询出现异常，原因：{}", e);
        }

        // 1.2 封装每个sku的信息
        Map<Long, Boolean> finalSkusHasStockMap = skusHasStockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            // 处理名称不一致字段
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            // 设置是否有库存
            skuEsModel.setHasStock(finalSkusHasStockMap == null || finalSkusHasStockMap.get(sku.getSkuId()));

            // 热度评分。默认设置为 0
            skuEsModel.setHotScore(0L);

            // 查询品牌名称，图标和分类名称
            BrandEntity brandEntity = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());

            // 设置对应的检索属性
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        // 将数据发送给检索服务 mymall-search es 进行保存
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode() == 0) {
            // 远程调用成功，修改当前 spu 的上架状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            // 远程调用失败
            // TODO: 重复调用？接口幂等性：重试机制
            // Feign 调用流程：
            /**
             * 1. 构造请求数据，将对象转为 json
             * 2. 发送请求进行执行（执行成功会解码响应数据）
             * 3. 执行请求会有重试机制
             */
        }
    }

}