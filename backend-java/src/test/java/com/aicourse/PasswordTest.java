package com.aicourse;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String encoded = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("Encoded: " + encoded);
        
        // 测试匹配
        boolean matches = encoder.matches(password, encoded);
        System.out.println("Matches: " + matches);
        
        // 测试我们之前使用的哈希
        String oldHash = "$2a$10$N.zmdr9k7uOCQb0bta/OPe8ecoCpldLfWLH0tH5GGmq9j3+WJDjfS";
        boolean oldMatches = encoder.matches(password, oldHash);
        System.out.println("Old hash matches: " + oldMatches);
    }
} 