package com.hzc.mymall.member.feign;

import com.hzc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-28 17:08
 */
// 声明要调用远程客户端 mymall-coupon
@FeignClient("mymall-coupon")
public interface CouponFeignService {

    // 要调用的远程接口（注意 url 路径必须要完整）
    @RequestMapping("coupon/coupon/member/list")
    public R membercoupons();
}
