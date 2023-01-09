package com.hzc.mymall.order.feign;

import com.hzc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-09 0:40
 */
@FeignClient("mymall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasStock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds);
}
