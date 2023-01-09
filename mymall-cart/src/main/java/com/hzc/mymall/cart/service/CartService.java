package com.hzc.mymall.cart.service;

import com.hzc.mymall.cart.vo.Cart;
import com.hzc.mymall.cart.vo.CartItem;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-07 13:53
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
