package com.hzc.mymall.cart.interceptor;

import com.hzc.common.constant.AuthServerConstant;
import com.hzc.common.constant.CartConstant;
import com.hzc.common.vo.MemberRespVo;
import com.hzc.mymall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * <p>
 *  在目标方法执行之前，判断用户的登录状态。
 *  并封装传递给 controller 目标请求
 * </p>
 *
 * @author hzc
 * @since 2023-01-07 14:37
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();

        MemberRespVo memberRespVo = (MemberRespVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(memberRespVo != null) {
            // 用户已登录
            userInfoTo.setUserId(memberRespVo.getId());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if(name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                    break;
                }
            }
        }

        if(StringUtils.isEmpty(userInfoTo.getUserKey())) {
            // 如果没有临时用户，需要创建一个临时用户
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        // 将userInfo设置到threadLocal里面
        threadLocal.set(userInfoTo);

        return true;
    }

    /**
     * 业务执行之后
     * 如果用户第一次登录，分配临时用户让浏览器保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();

        // 如果没有临时用户，需要保存一个临时用户给浏览器
        if(!userInfoTo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("mymall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
