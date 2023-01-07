package com.hzc.mymall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hzc.mymall.product.entity.AttrEntity;
import com.hzc.mymall.product.service.AttrService;
import com.hzc.mymall.product.vo.AttrGroupWithAttrsVo;
import com.hzc.mymall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.product.dao.AttrGroupDao;
import com.hzc.mymall.product.entity.AttrGroupEntity;
import com.hzc.mymall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if(categoryId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );
            return new PageUtils(page);
        }else {
            queryWrapper.eq("catelog_id", categoryId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类id，查找所有分组以及分组下的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 1. 查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 2. 查询所有属性
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> relationAttrs = attrService.getRelationAttr(item.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(relationAttrs);

            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());

        return attrGroupWithAttrsVos;
    }

    /**
     * 查询当前spu对应的所有属性分组信息
     * @return
     * @param spuId
     * @param catalogId
     */
    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1. 查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
        List<SpuItemAttrGroupVo> vos = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);

        return vos;
    }

}