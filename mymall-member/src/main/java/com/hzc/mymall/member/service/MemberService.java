package com.hzc.mymall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzc.common.utils.PageUtils;
import com.hzc.mymall.member.entity.MemberEntity;
import com.hzc.mymall.member.exception.PhoneExistException;
import com.hzc.mymall.member.exception.UserNameExistException;
import com.hzc.mymall.member.vo.MemberRegisterVo;
import com.hzc.mymall.member.vo.MemberUserLoginVo;
import com.hzc.mymall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author hzc
 * @email hzc@gmail.com
 * @date 2022-12-28 14:38:05
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String userName) throws UserNameExistException;

    MemberEntity login(MemberUserLoginVo vo);

    MemberEntity login(SocialUser socialUser);
}

