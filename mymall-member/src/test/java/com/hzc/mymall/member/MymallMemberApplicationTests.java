package com.hzc.mymall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class MymallMemberApplicationTests {

    @Test
    public void test() {
        String md5Hex = DigestUtils.md5Hex("123456");
        System.out.println(md5Hex);

        // 盐值加密
        String md5Crypt = Md5Crypt.md5Crypt("123456".getBytes(), "$1$aaaaaaaa");
        System.out.println(md5Crypt);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$mINYiOEFdhrmpz21.2.zY.i.RZfrWFEZapq71iFd0vSFd73.hXUqG");
        System.out.println(encode);
        System.out.println(matches);
    }
}
