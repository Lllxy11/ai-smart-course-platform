package com.aicourse.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;
    
    private String testUsername = "testuser";
    private Long testUserId = 123L;

    @Test
    @DisplayName("生成Token - 正常情况")
    void testGenerateToken_Success() {
        // When
        String token = jwtUtils.generateToken(testUserId, testUsername);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT应该有3个部分
    }

    @Test
    @DisplayName("从Token提取用户ID - 正常情况")
    void testGetUserIdFromToken_Success() {
        // Given
        String token = jwtUtils.generateToken(testUserId, testUsername);

        // When
        Long extractedUserId = jwtUtils.getUserIdFromToken(token);

        // Then
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    @DisplayName("从Token提取用户名 - 正常情况")
    void testGetUsernameFromToken_Success() {
        // Given
        String token = jwtUtils.generateToken(testUserId, testUsername);

        // When
        String extractedUsername = jwtUtils.getUsernameFromToken(token);

        // Then
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    @DisplayName("验证Token - 有效Token")
    void testValidateToken_ValidToken() {
        // Given
        String token = jwtUtils.generateToken(testUserId, testUsername);

        // When
        boolean isValid = jwtUtils.validateToken(token, testUserId);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("检查Token是否过期 - 未过期")
    void testIsTokenExpired_NotExpired() {
        // Given
        String token = jwtUtils.generateToken(testUserId, testUsername);

        // When
        boolean isExpired = jwtUtils.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }
} 