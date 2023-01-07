package com.hzc.mymall.member.exception;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-05 17:21
 */
public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号已存在");
    }
}
