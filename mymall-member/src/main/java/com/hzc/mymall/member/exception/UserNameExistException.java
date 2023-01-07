package com.hzc.mymall.member.exception;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-05 17:21
 */
public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户名已存在");
    }
}
