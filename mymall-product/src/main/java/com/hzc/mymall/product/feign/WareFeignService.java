package com.hzc.mymall.product.feign;

import com.hzc.common.to.SkuHasStockVo;
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
 * @since 2023-01-01 0:47
 */
@FeignClient("mymall-ware")
public interface WareFeignService {

    @PostMapping("ware/waresku/hasStock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds);
}
