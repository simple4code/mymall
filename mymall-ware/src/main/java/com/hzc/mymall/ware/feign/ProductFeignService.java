package com.hzc.mymall.ware.feign;

import com.hzc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-31 15:02
 */
@FeignClient("mymall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
