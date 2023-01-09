package com.hzc.mymall.cart.controller;

import com.hzc.common.constant.AuthServerConstant;
import com.hzc.mymall.cart.interceptor.CartInterceptor;
import com.hzc.mymall.cart.service.CartService;
import com.hzc.mymall.cart.vo.Cart;
import com.hzc.mymall.cart.vo.CartItem;
import com.hzc.mymall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-07 14:13
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems() {

        return cartService.getUserCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.mymall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.mymall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);

        return "redirect:http://cart.mymall.com/cart.html";
    }

    /**
     * 去购物车页面
     * 浏览器有一个cookie：user-key；标识用户身份，一个月后过期；
     * 如果第一次使用 jd 的购物车功能，都会给一个临时的用户身份；
     * 浏览器以后保存，每次访问都会带上这个cookie
     *
     * 已登录：session 有
     * 未登录：按照 cookie 里面带来的 user-key 来做
     * 第一次登录：如果没有临时用户，需要创建一个临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
        // 从 threadLocal 快速获取用户信息
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        System.out.println(userInfoTo);

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) {
        cartService.addToCart(skuId, num);

        ra.addAttribute("skuId", skuId);
        // 添加购物车成功后，重定向到成功页面，防止多次刷新url改变商品添加数量
        return "redirect:http://cart.mymall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(Long skuId, Model model) {
        // 重定向到成功页面，再次查询购物车数据
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
