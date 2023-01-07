package com.hzc.mymall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.hzc.common.valid.AddGroup;
import com.hzc.common.valid.ListValue;
import com.hzc.common.valid.UpdateGroup;
import com.hzc.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 13:26:13
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class})
	@Null(message = "新增不能指定id", groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交", groups = {UpdateGroup.class, AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "logo必须是合法url地址", groups = {UpdateGroup.class, AddGroup.class})
	@NotBlank(groups = {AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(vals = {0,1}, groups = {UpdateStatusGroup.class, AddGroup.class})
	@NotNull(groups = {UpdateStatusGroup.class, AddGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须为字母", groups = {UpdateGroup.class, AddGroup.class})
	@NotEmpty(groups = {AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0l, message = "排序字段必须大于等于0", groups = {UpdateGroup.class, AddGroup.class})
	@NotNull(groups = {AddGroup.class})
	private Integer sort;

}
