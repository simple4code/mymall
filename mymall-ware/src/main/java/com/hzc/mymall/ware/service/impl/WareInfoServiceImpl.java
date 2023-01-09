package com.hzc.mymall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hzc.common.utils.R;
import com.hzc.mymall.ware.feign.MemberFeignService;
import com.hzc.mymall.ware.vo.FareVo;
import com.hzc.mymall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.ware.dao.WareInfoDao;
import com.hzc.mymall.ware.entity.WareInfoEntity;
import com.hzc.mymall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String)params.get("key");
        if(StringUtils.isNotEmpty(key)) {
            queryWrapper.eq("id", key).or().like("name", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据收货地址计算运费
     * @param addrId
     * @return
     */
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (data != null) {
            String phone = data.getPhone();
            String fare = phone.substring(phone.length() - 1);
            fareVo.setAddress(data);
            fareVo.setFare(new BigDecimal(fare));
            return fareVo;
        }
        return null;
    }

}