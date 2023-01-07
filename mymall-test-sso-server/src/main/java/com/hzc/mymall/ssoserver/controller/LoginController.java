package com.hzc.mymall.ssoserver.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-06 22:00
 */
@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/userInfo")
    @ResponseBody
    public String userInfo(@RequestParam("token") String token) {
        return stringRedisTemplate.opsForValue().get(token);
    }

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model,
                            @CookieValue(value = "sso-token", required = false) String sso_token) {
        if(!StringUtils.isEmpty(sso_token)) {
            // 已经有用户登录过
            return "redirect:" + url + "?token=" + sso_token;
        }
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(String username, String password, String url,
                          HttpServletResponse response) {
        if(!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            // 登录成功跳转，跳回到之前的页面
            // 需要把登录成功的用户保存起来（session 或者 redis）
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);
            Cookie cookie = new Cookie("sso-token", uuid);
            response.addCookie(cookie);

            return "redirect:" + url + "?token=" + uuid;
        }
        // 登录失败，留在登录页
        return "login";
    }
}
