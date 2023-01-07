package com.hzc.mymall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-06 21:46
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    /**
     * 无需登录即可访问
     * @return
     */
    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }

    /**
     * 需要登录才可访问
     * 需要感知此次登录是否是从ssoserver登录成功跳转回来的
     * @param model
     * @return
     */
    @GetMapping("/employees")
    public String employees(Model model, HttpSession httpSession,
                            @RequestParam(value = "token", required = false) String token) {
        if(!StringUtils.isEmpty(token)) {
            // 去 ssoserver 登录成功跳转回来就会带上 token
            // 去 ssoserver 获取当前 token 真正对应的用户
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> entity =
                    restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = entity.getBody();
            httpSession.setAttribute("loginUser", body);
        }

        Object loginUser = httpSession.getAttribute("loginUser");
        if(loginUser == null) {
            // 未登录，跳转到登录服务器进行登录
            // 跳转到登录服务器后，url上的参数redirect_url标识自己是哪个页面
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client1.com:8081/employees";
        }else {
            List<String> emps = new ArrayList<>();
            emps.add("Jack");
            emps.add("Tom");
            model.addAttribute("emps", emps);
            return "list";
        }
    }
}
