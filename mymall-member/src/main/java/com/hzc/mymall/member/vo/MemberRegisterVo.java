package com.hzc.mymall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-05 17:06
 */
@Data
public class MemberRegisterVo {
    private String userName;

    private String password;

    private String phone;
}
