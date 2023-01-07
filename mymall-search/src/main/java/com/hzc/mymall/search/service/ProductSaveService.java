package com.hzc.mymall.search.service;

import com.hzc.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-01 1:25
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
