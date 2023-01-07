package com.hzc.mymall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hzc.common.constant.AuthServerConstant;
import com.hzc.common.utils.HttpUtils;
import com.hzc.common.utils.R;
import com.hzc.mymall.auth.feign.MemberFeignService;
import com.hzc.common.vo.MemberRespVo;
import com.hzc.mymall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  处理社交登录请求
 * </p>
 *
 * @author hzc
 * @since 2023-01-05 23:55
 */
@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 社交登录成功回调
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/oauth2.0/github/success")
    public String github(@RequestParam("code") String code, HttpSession httpSession) throws Exception {
        // 1. 根据 code 换取 accessToken
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("client_id", "38203b5c5129ac0dfd68");
        paramsMap.put("client_secret", "31a0b7a3eb604b4a300c7535780d04b4d67d0f12");
        paramsMap.put("code", code);
        paramsMap.put("redirect_uri", "http://auth.mymall.com/oauth2.0/github/success");
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Accept", "application/json");

        HttpResponse response = HttpUtils.doPost("https://github.com/",
                "/login/oauth/access_token",
                "post",
                headersMap,
                paramsMap,
                Collections.emptyMap());
        // 2. 处理响应结果
        if(response.getStatusLine().getStatusCode() == 200) {
            // 获取到了 accessToken
            String responseJson = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(responseJson, SocialUser.class);
            // 使用 accessToken 获取用户的id
            headersMap.put("Authorization", socialUser.getToken_type() + " " + socialUser.getAccess_token());
            response = HttpUtils.doPost("https://api.github.com/",
                    "/user",
                    "get",
                    headersMap,
                    Collections.emptyMap(),
                    Collections.emptyMap());
            if(response.getStatusLine().getStatusCode() == 200) {
                responseJson = EntityUtils.toString(response.getEntity());
                Map<String, Object> map = JSON.parseObject(responseJson, new TypeReference<Map<String, Object>>(){});
                socialUser.setUid(String.valueOf(map.get("id")));
                System.out.println(socialUser);
            }
            // 获取当前登录的社交用户
            // 1), 当前用户如果是新用户（第一次登录网站），就自动为该用户注册一个会员信息账号，以后这个社交账号就对应指定的会员
            R oauth2LoginResponse = memberFeignService.oauth2Login(socialUser);
            if(oauth2LoginResponse.getCode() == 0) {
                MemberRespVo memberRespVo = oauth2LoginResponse.getData(new TypeReference<MemberRespVo>() {});
                log.info("登录成功，用户信息是：{}", memberRespVo.toString());
                // todo 1，默认发的令牌。session=dsajkdjl。作用域：当前域：（解决子域session共享问题）
                // todo 2，使用JSON的序列化方式来序列化对象数据到redis中
                httpSession.setAttribute(AuthServerConstant.LOGIN_USER, memberRespVo);
                // 第一次设置 session 的使用，就应该配置使得子域和父域都能使用同一session，这里会采用SpringSession配置实现
                // 3. 登录成功跳到首页
                return "redirect:http://mymall.com";
            }else {
                // 登录失败
                return "redirect:http://auth.mymall.com/login.html";
            }
        }else {
            // 登录失败
            return "redirect:http://auth.mymall.com/login.html";
        }
    }
}
