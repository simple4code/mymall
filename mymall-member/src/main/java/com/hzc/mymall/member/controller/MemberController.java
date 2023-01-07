package com.hzc.mymall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.hzc.common.exception.BizCodeEnum;
import com.hzc.mymall.member.exception.PhoneExistException;
import com.hzc.mymall.member.exception.UserNameExistException;
import com.hzc.mymall.member.feign.CouponFeignService;
import com.hzc.mymall.member.vo.MemberRegisterVo;
import com.hzc.mymall.member.vo.MemberUserLoginVo;
import com.hzc.mymall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hzc.mymall.member.entity.MemberEntity;
import com.hzc.mymall.member.service.MemberService;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.R;



/**
 * 会员
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:38:05
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    // Coupons远程调用服务
    @Autowired
    private CouponFeignService couponFeignService;

    /**
     * 社交用户登录
     * @return
     */
    @PostMapping("/oauth2/login")
    public R oauth2Login(@RequestBody SocialUser socialUser) {
        MemberEntity memberEntity = memberService.login(socialUser);
        if(memberEntity != null) {
            // 登录成功
            return R.ok().setData(memberEntity);
        }
        return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMessage());
    }

    /**
     * 用户登录
     * @return
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberUserLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if(memberEntity != null) {
            // 登录成功
            return R.ok().setData(memberEntity);
        }
        return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMessage());
    }

    /**
     * 用户注册
     * @param vo
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        }catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        }catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }

        return R.ok();
    }

    // 远程调用测试
    @RequestMapping("/coupons")
    public R feignTest() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("Jack");
        R memberCoupons = couponFeignService.membercoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
