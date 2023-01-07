package com.hzc.mymall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.hzc.common.valid.AddGroup;
import com.hzc.common.valid.UpdateGroup;
import com.hzc.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hzc.mymall.product.entity.BrandEntity;
import com.hzc.mymall.product.service.BrandService;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.R;


/**
 * 品牌
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 13:26:13
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /*, BindingResult bindingResult*/){
        // 判断校验是否通过
        // 使用统一异常处理后，可以不用做下面的校验
//        if(bindingResult.hasErrors()) {
//            Map<String, String> errors = new HashMap<>();
//            // 获取错误的校验结果
//            bindingResult.getFieldErrors().forEach((item) -> {
//                // 获取错误的提示信息
//                String message = item.getDefaultMessage();
//                // 获取错误的属性名称
//                String field = item.getField();
//                errors.put(field, message);
//            });
//
//            return R.error(400, "提交的数据不合法").put("data", errors);
//        }

		brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
