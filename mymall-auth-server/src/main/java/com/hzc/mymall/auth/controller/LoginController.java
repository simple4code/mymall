package com.hzc.mymall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.hzc.common.constant.AuthServerConstant;
import com.hzc.common.exception.BizCodeEnum;
import com.hzc.common.utils.R;
import com.hzc.common.vo.MemberRespVo;
import com.hzc.mymall.auth.feign.MemberFeignService;
import com.hzc.mymall.auth.feign.ThirdPartyFeignService;
import com.hzc.mymall.auth.vo.UserLoginVo;
import com.hzc.mymall.auth.vo.UserRegisterVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-04 22:56
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {

        // 1, 接口防刷
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(StringUtils.isNotEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis() - l < 60*1000) {
                // 60秒内不能重发验证码
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        // 2, 验证码的再次校验
        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        // redis 缓存验证码，以供再次校验，防止同一个 phone 在60秒内再次发送验证码
        stringRedisTemplate
                .opsForValue()
                .set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone, code, 10, TimeUnit.MINUTES);

        thirdPartyFeignService.sendCode(phone, code.split("_")[0]);
        return R.ok();
    }

    /**
     * todo: 重定向携带数据，利用 session 原理。将数据放在 session 中。只要跳到下一个页面取出这个数据之后，session里面的数据就会删除
     * todo: 需要解决分布式下 session 问题
     * @param userRegisterVo
     * @param bindingResult
     * @param redirectAttributes    模拟重定向携带数据
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
//            Map<String, String> errors = new HashMap<>();
//
//            bindingResult.getFieldErrors().stream().forEach(item -> {
//                String field = item.getField();
//                String msg = item.getDefaultMessage();
//                errors.put(field, msg);
//            });

            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错，转发回注册页
            // 由于配置了默认视图控制器，所以这里的请求会被发到默认视图控制器里面，但是这个请求是转发的，
            // 所以还是Post请求，而默认视图控制器里面的/reg.html是Get请求，所以会报405错误，
            // 所以这里使用重定向返回 reg.html 就好，不用转发形式
//            return "forward:/reg.html";
            return "redirect:http://auth.mymall.com/reg.html";
        }

        // 1. 校验验证码
        String code = userRegisterVo.getCode();
        String phone = userRegisterVo.getPhone();
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone);
        if(StringUtils.isEmpty(redisCode) || !code.equals(redisCode.split("_")[0])) {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mymall.com/reg.html";
        }
        // 验证码正确，删除缓存验证码（令牌机制）
        stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone);
        // 调用远程服务进行注册
        R registerResponse = memberFeignService.register(userRegisterVo);
        if(registerResponse.getCode() == 0) {
            // 注册成功回到登录页
            return "redirect:http://auth.mymall.com/login.html";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String) registerResponse.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);
            // 注册失败，回到注册页面
            return "redirect:http://auth.mymall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes,
                        HttpSession httpSession) {
        R loginResponse = memberFeignService.login(vo);
        if(loginResponse.getCode() == 0) {
            // 登录成功进入商城首页
            MemberRespVo memberRespVo = loginResponse.getData(new TypeReference<MemberRespVo>() {});
            httpSession.setAttribute(AuthServerConstant.LOGIN_USER, memberRespVo);
            return "redirect:http://mymall.com";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", (String) loginResponse.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);
            // 登录失败，回到登录页面
            return "redirect:http://auth.mymall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession httpSession) {
        Object attribute = httpSession.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null) {
            // 如果已登录，重定向回首页
            return "redirect:http://mymall.com";
        }
        return "login";
    }
}
