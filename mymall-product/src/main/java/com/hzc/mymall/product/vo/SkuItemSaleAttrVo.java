package com.hzc.mymall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description:
 * @createTime: 2020-06-19 18:17
 **/

@Data
@ToString
public class SkuItemSaleAttrVo {

    private Long attrId;

    private String attrName;

    private List<AttrValueWithSkuIdVo> attrValues;

}
