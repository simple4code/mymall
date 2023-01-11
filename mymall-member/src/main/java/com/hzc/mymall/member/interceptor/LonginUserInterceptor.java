package com.hzc.mymall.member.interceptor;

import com.hzc.common.constant.AuthServerConstant;
import com.hzc.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-08 17:44
 */
@Component
public class LonginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Feign 服务之间的调用不需要拦截
        String uri = request.getRequestURI();
        boolean ignoreMatch = new AntPathMatcher().match("/member/**", uri);
        if(ignoreMatch) {
            return true;
        }

        HttpSession session = request.getSession();
        MemberRespVo attribute = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null) {
            loginUser.set(attribute);
            String message = (String) request.getSession().getAttribute("msg");
            if(message != null && message.equals("请先进行登录")) {
                request.getSession().removeAttribute("msg");
            }
            // 已登录，放行
            return true;
        }
        request.getSession().setAttribute("msg", "请先进行登录");
        response.sendRedirect("http://auth.mymall.com/login.html");
        return false;
    }
}
