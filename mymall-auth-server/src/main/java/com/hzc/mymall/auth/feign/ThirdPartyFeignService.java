package com.hzc.mymall.auth.feign;

import com.hzc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-05 14:56
 */
@FeignClient("mymall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
