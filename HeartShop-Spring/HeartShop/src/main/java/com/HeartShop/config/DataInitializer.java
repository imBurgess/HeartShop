package com.HeartShop.config;

import com.HeartShop.entity.Member;
import com.HeartShop.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 應用程式啟動時建立預設管理員帳號（若不存在）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        if (memberMapper.selectByName("admin").isEmpty()) {
            Member admin = new Member();
            admin.setEmail("admin");
            admin.setName("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setSubscribeEdm(false);
            memberMapper.insertMember(admin);
            log.info("預設管理員帳號已建立：帳號 admin / 密碼 admin");
        } else {
            log.debug("預設管理員帳號已存在，略過初始化");
        }
    }
}
