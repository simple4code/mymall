package com.hzc.mymall.product.web;

import com.hzc.mymall.product.service.SkuInfoService;
import com.hzc.mymall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 *  商品详情信息控制器
 * </p>
 *
 * @author hzc
 * @since 2023-01-04 17:00
 */
@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 展示当前 sku 详情
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {

        System.out.println("准备查询" + skuId + "详情");
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);

        return "item";
    }
}
