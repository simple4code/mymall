package com.hzc.mymall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.hzc.common.to.es.SkuEsModel;
import com.hzc.mymall.search.config.MyMallElasticsearchConfig;
import com.hzc.mymall.search.constant.EsConstant;
import com.hzc.mymall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-01 1:25
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到 es
        // 1. 给 es 中建立索引 product，并建立好映射关系

        // 2. 给 es 中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            // 构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, MyMallElasticsearchConfig.COMMON_OPTIONS);

        // 如果批量保存数据出错
        boolean hasFailures = bulk.hasFailures();
        if(hasFailures) {
            List<String> ids = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
            log.error("商品上架错误: {}", ids);
        }
        log.info("商品上架完成, 返回数据：{}", bulk);

        return !hasFailures;
    }
}
