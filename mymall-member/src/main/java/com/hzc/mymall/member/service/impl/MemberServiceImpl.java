package com.hzc.mymall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hzc.common.utils.HttpUtils;
import com.hzc.mymall.member.dao.MemberLevelDao;
import com.hzc.mymall.member.entity.MemberLevelEntity;
import com.hzc.mymall.member.exception.PhoneExistException;
import com.hzc.mymall.member.exception.UserNameExistException;
import com.hzc.mymall.member.vo.MemberRegisterVo;
import com.hzc.mymall.member.vo.MemberUserLoginVo;
import com.hzc.mymall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzc.common.utils.PageUtils;
import com.hzc.common.utils.Query;

import com.hzc.mymall.member.dao.MemberDao;
import com.hzc.mymall.member.entity.MemberEntity;
import com.hzc.mymall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 注册新用户
     * @param vo
     */
    @Override
    public void register(MemberRegisterVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        if(levelEntity != null) {
            memberEntity.setLevelId(levelEntity.getId());
        }

        // 检查用户名和手机号是否唯一
        // 如果不唯一，使用异常机制抛出异常让controller层感知到
        checkUserNameUnique(vo.getUserName());
        checkPhoneUnique(vo.getPhone());

        memberEntity.setUsername(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());
        memberEntity.setNickname(vo.getUserName());

        // MD5 + 加盐 方式加密密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(count != 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if(count != 0) {
            throw new UserNameExistException();
        }
    }

    /**
     * 用户登录
     * @param vo
     * @return
     */
    @Override
    public MemberEntity login(MemberUserLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        // 从数据库中根据登录名称或者手机号查询记录
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct)
                .or()
                .eq("mobile", loginacct));
        if(memberEntity == null) {
            // 登录失败
            return null;
        }
        String passwordDb = memberEntity.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(password, passwordDb);
        if(!matches) {
            // 密码错误，登录失败
            return null;
        }

        return memberEntity;
    }

    /**
     * 社交账号登录
     * @param socialUser
     * @return
     */
    @Override
    public MemberEntity login(SocialUser socialUser) {
        // 登录和注册合并逻辑
        String uid = socialUser.getUid();
        // 1. 判断当前社交用户是否已登录过系统
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(memberEntity != null) {
            // 该社交用户已注册过
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());

            memberDao.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setSocialUid(socialUser.getUid());

            return memberEntity;
        }else {
            // 2. 没有查到当前社交用户对应的记录，需要为当前社交用户注册
            MemberEntity registerEntity = new MemberEntity();
            try {
                // 3. 查询当前社交用户的社交账号信息（昵称，性别等）-- 即使查询失败也不影响
                Map<String, String> headersMap = new HashMap<>();
                headersMap.put("Authorization", socialUser.getToken_type() + " " + socialUser.getAccess_token());
                HttpResponse response = HttpUtils.doPost("https://api.github.com/",
                        "/user",
                        "get",
                        headersMap,
                        Collections.emptyMap(),
                        Collections.emptyMap());
                if(response.getStatusLine().getStatusCode() == 200) {
                    String responseJson = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(responseJson);
                    // ...
                    registerEntity.setNickname(jsonObject.getString("login"));
                    // ...
                }
            }catch (Exception e) {}
            registerEntity.setSocialUid(socialUser.getUid());
            registerEntity.setAccessToken(socialUser.getAccess_token());
            memberDao.insert(registerEntity);

            return registerEntity;
        }
    }

}