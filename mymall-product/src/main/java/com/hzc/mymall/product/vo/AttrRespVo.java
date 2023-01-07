package com.hzc.mymall.product.vo;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2022-12-30 17:00
 */
@Data
public class AttrRespVo extends AttrVo{
    /**
     * 分类名称
     */
    private String catelogName;
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 分类路径
     */
    private Long[] catelogPath;
}
