package com.hzc.mymall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-07 14:40
 */
@Data
@ToString
public class UserInfoTo {
    /**
     * 登录会设置 userId
     */
    private Long userId;
    /**
     * 未登录会设置 userKey
     */
    private String userKey;

    /**
     * 是否是临时用户
     */
    private boolean tempUser;
}
