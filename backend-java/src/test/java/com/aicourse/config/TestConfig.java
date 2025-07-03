package com.aicourse.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * 测试配置类
 * 提供测试环境需要的Bean配置
 */
@TestConfiguration
@TestPropertySource(locations = "classpath:application-test.yml")
public class TestConfig {

    /**
     * 密码编码器
     * 测试环境使用较低的强度以提升测试速度
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4); // 使用较低强度以提升测试速度
    }
} 