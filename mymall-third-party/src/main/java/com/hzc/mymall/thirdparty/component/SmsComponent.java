package com.hzc.mymall.thirdparty.component;

import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-05 0:11
 */
@Component
public class SmsComponent {

    public void sendSmsCode(String phone, String code) {
        System.out.println("发送验证码 " + code + " 给 " + phone);
    }
}
