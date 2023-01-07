package com.hzc.mymall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hzc.mymall.product.dao.AttrGroupDao;
import com.hzc.mymall.product.dao.SkuSaleAttrValueDao;
import com.hzc.mymall.product.entity.BrandEntity;
import com.hzc.mymall.product.service.BrandService;
import com.hzc.mymall.product.service.CategoryService;
import com.hzc.mymall.product.vo.SkuItemSaleAttrVo;
import com.hzc.mymall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MymallProductApplicationTests {

	@Autowired
	private BrandService brandService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private AttrGroupDao attrGroupDao;

	@Autowired
	private SkuSaleAttrValueDao skuSaleAttrValueDao;

	@Test
	public void test() {
		List<SpuItemAttrGroupVo> group = attrGroupDao.getAttrGroupWithAttrsBySpuId(13L, 225L);
		System.out.println(group);
	}

	@Test
	public void test2() {
		List<SkuItemSaleAttrVo> attrs = skuSaleAttrValueDao.getSaleAttrsBySpuId(13L);
		System.out.println(attrs);
	}

	@Test
	public void testBrandService() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setDescript("测试品牌1");
		brandEntity.setName("测试品牌名称");
		brandService.save(brandEntity);
		System.out.println("保存成功");
	}

	@Test
	public void testBrandService2() {
		List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().gt("brand_id", 1L));
		list.forEach(brand -> System.out.println(brand));
	}

	@Test
	public void testFindCatelogPath() {
		Long[] catelogPath = categoryService.findCatelogPath(255L);
		log.info("完整路径：{}", Arrays.toString(catelogPath));
	}
}
