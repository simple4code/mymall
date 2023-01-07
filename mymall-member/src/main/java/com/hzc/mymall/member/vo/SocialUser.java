package com.hzc.mymall.member.vo;

import lombok.Data;
import lombok.ToString;

/**
 * <p>
 *  社交用户实体类
 * </p>
 *
 * @author hzc
 * @since 2023-01-06 0:24
 */
@Data
@ToString
public class SocialUser {
    private String access_token;
    private String scope;
    private String token_type;
    private String uid;
}
