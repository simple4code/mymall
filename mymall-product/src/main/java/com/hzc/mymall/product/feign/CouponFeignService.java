package com.hzc.mymall.product.feign;

import com.hzc.common.to.SkuReductionTo;
import com.hzc.common.to.SpuBoundTo;
import com.hzc.common.utils.R;
import com.hzc.mymall.product.vo.Bounds;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-30 22:35
 */
@FeignClient("mymall-coupon")
public interface CouponFeignService {

    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
