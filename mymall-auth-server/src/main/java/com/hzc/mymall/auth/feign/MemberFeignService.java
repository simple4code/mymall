package com.hzc.mymall.auth.feign;

import com.hzc.common.utils.R;
import com.hzc.mymall.auth.vo.SocialUser;
import com.hzc.mymall.auth.vo.UserLoginVo;
import com.hzc.mymall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 *
 * </p>
 *
 * @author hzc
 * @since 2023-01-05 18:06
 */
@FeignClient("mymall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    public R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    public R oauth2Login(@RequestBody SocialUser socialUser);
}
