package com.hzc.mymall.product.feign;

import com.hzc.common.to.es.SkuEsModel;
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
 * @since 2023-01-01 1:47
 */
@FeignClient("mymall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
