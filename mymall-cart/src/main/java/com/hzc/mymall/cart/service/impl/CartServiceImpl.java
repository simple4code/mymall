package com.hzc.mymall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hzc.common.constant.CartConstant;
import com.hzc.common.utils.R;
import com.hzc.mymall.cart.feign.ProductFeignService;
import com.hzc.mymall.cart.interceptor.CartInterceptor;
import com.hzc.mymall.cart.service.CartService;
import com.hzc.mymall.cart.vo.Cart;
import com.hzc.mymall.cart.vo.CartItem;
import com.hzc.mymall.cart.vo.SkuInfoVo;
import com.hzc.mymall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-07 13:53
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 给购物车添加商品
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        // 判断购物车是否已有该商品
        String itemJson = (String) cartOps.get(skuId.toString());
        if(StringUtils.isEmpty(itemJson)) {
            CartItem cartItem = new CartItem();
            // 2, 将商品信息添加到购物车
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                // 1, 远程查询添加到购物车里面的商品信息
                R response = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = response.getData("skuInfo", new TypeReference<SkuInfoVo>(){});

                cartItem.setSkuId(skuId);
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
            }, executor);

            // 3, 远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrTask = CompletableFuture.runAsync(() -> {
                List<String> skuAttrs = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuAttrs);
            }, executor);

            try {
                CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrTask).get();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            String cartItemJson = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), cartItemJson);

            return cartItem;
        }else {
            // 购物车已有此商品，修改商品数量
            CartItem cartItem = JSON.parseObject(itemJson, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            // 重新存储商品
            String cartItemJson = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), cartItemJson);

            return cartItem;
        }
    }

    /**
     * 获取购物车中某个购物项
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String itemJson = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(itemJson, CartItem.class);
    }

    /**
     * 获取购物车数据
     * @return
     */
    @Override
    public Cart getCart() {
        Cart cart = new Cart();

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() != null) {
            // 已登录
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
            // 如果存在临时购物车且还未合并，需要进行合并
            String tempCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if(tempCartItems != null) {
                // 临时购物车有数据，需要合并
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                // 合并完成，清除临时购物车数据
                clearCart(tempCartKey);
            }
            // 获取登录后的购物车数据【包含合并的临时购物车数据，和登录后的购物车数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else {
            // 未登录
            String userKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            // 获取临时购物车的购物项
            List<CartItem> cartItems = getCartItems(userKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 获取要操作的购物车对应的 hashOperations
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        // 从 threadLocal 快速获取用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey = "";
        if(userInfoTo.getUserId() != null) {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        }else {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOperations = stringRedisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> objects = hashOps.values();
        if(objects != null && !objects.isEmpty()) {
            List<CartItem> collect = objects.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 清空购物车
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物车列表选项
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String cartItemJson = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), cartItemJson);
    }

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String cartItemJson = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), cartItemJson);
    }

    /**
     * 删除购物项
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}
