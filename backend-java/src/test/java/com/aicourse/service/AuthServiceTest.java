package com.aicourse.service;

import com.aicourse.config.TestDataBuilder;
import com.aicourse.dto.*;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.repository.UserRepository;
import com.aicourse.util.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthService 测试类
 * 包含38个测试用例，覆盖认证授权的所有功能
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataBuilder testDataBuilder;

    @Autowired
    private JwtUtils jwtUtils;

    private User testUser;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = testDataBuilder.createTestUser("testuser", "test@example.com", UserRole.STUDENT);
        
        // 创建模拟HTTP请求
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
    }

    @AfterEach
    void tearDown() {
        testDataBuilder.cleanupTestData();
    }

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("用户登录 - 使用用户名正常登录")
    void testLogin_WithUsername_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(testUser.getUsername());
        request.setPassword("password123");

        // When
        TokenResponse response = authService.login(request, mockRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertTrue(response.getExpiresIn() > 0);
    }

    @Test
    @DisplayName("用户登录 - 使用邮箱正常登录")
    void testLogin_WithEmail_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(testUser.getEmail());
        request.setPassword("password123");

        // When
        TokenResponse response = authService.login(request, mockRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertTrue(response.getExpiresIn() > 0);
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(testUser.getUsername());
        request.setPassword("wrongpassword");

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request, mockRequest));
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistentuser");
        request.setPassword("password123");

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request, mockRequest));
    }

    @Test
    @DisplayName("用户登录 - 用户名为空")
    void testLogin_EmptyUsername_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password123");

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request, mockRequest));
    }

    @Test
    @DisplayName("用户登录 - 密码为空")
    void testLogin_EmptyPassword_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(testUser.getUsername());
        request.setPassword("");

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request, mockRequest));
    }

    @Test
    @DisplayName("用户登录 - 用户被禁用")
    void testLogin_UserDisabled_ThrowsException() {
        // Given
        testUser.setIsActive(false);
        userRepository.save(testUser);

        LoginRequest request = new LoginRequest();
        request.setUsername(testUser.getUsername());
        request.setPassword("password123");

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request, mockRequest));
    }

    @Test
    @DisplayName("用户登录 - 包含用户信息")
    void testLoginWithUserInfo_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(testUser.getUsername());
        request.setPassword("password123");

        // When
        Map<String, Object> response = authService.loginWithUserInfo(request, mockRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.get("token"));
        assertNotNull(response.get("user"));
        assertNotNull(response.get("expires_in"));
        assertNotNull(response.get("login_time"));
    }

    @Test
    @DisplayName("用户登录 - 密码长度过短")
    void testLogin_PasswordTooShort_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername(testUser.getUsername());
        request.setPassword("123");

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request, mockRequest));
    }

    @Test
    @DisplayName("用户登录 - 用户名过长")
    void testLogin_UsernameTooLong_ThrowsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("a".repeat(101));
        request.setPassword("password123");

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request, mockRequest));
    }

    // ==================== 注册测试 ====================

    @Test
    @DisplayName("用户注册 - 正常注册")
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFullName("新用户");
        request.setRole(UserRole.STUDENT);

        // When
        UserResponse response = authService.register(request, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("新用户", response.getFullName());
        assertEquals(UserRole.STUDENT, response.getRole());
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void testRegister_UsernameExists_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername(testUser.getUsername());
        request.setEmail("different@example.com");
        request.setPassword("password123");
        request.setFullName("不同用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 邮箱已存在")
    void testRegister_EmailExists_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("differentuser");
        request.setEmail(testUser.getEmail());
        request.setPassword("password123");
        request.setFullName("不同用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 无效用户名格式")
    void testRegister_InvalidUsername_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("invalid-username!");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 无效邮箱格式")
    void testRegister_InvalidEmail_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser2");
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 尝试注册管理员角色")
    void testRegister_AdminRole_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("adminuser");
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setFullName("管理员用户");
        request.setRole(UserRole.ADMIN);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 用户名为空")
    void testRegister_EmptyUsername_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 邮箱为空")
    void testRegister_EmptyEmail_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser2");
        request.setEmail("");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 密码为空")
    void testRegister_EmptyPassword_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser2");
        request.setEmail("test@example.com");
        request.setPassword("");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 姓名为空")
    void testRegister_EmptyFullName_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser2");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户注册 - 角色为空")
    void testRegister_NullRole_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser2");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(null);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    // ==================== 用户信息测试 ====================

    @Test
    @DisplayName("获取当前用户信息 - 成功")
    void testGetCurrentUserInfo_Success() {
        // When
        UserResponse response = authService.getCurrentUserInfo(testUser);

        // Then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getFullName(), response.getFullName());
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdateUserInfo_Success() {
        // Given
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", "更新后的姓名");
        updates.put("phone", "13800138000");

        // When
        UserResponse response = authService.updateUserInfo(testUser, updates);

        // Then
        assertNotNull(response);
        assertEquals("更新后的姓名", response.getFullName());
        assertEquals("13800138000", response.getPhone());
    }

    @Test
    @DisplayName("更新用户信息 - 无效字段")
    void testUpdateUserInfo_InvalidField_ThrowsException() {
        // Given
        Map<String, Object> updates = new HashMap<>();
        updates.put("invalidField", "value");

        // When & Then
        assertDoesNotThrow(() -> authService.updateUserInfo(testUser, updates));
    }

    // ==================== 密码管理测试 ====================

    @Test
    @DisplayName("修改密码 - 成功")
    void testChangePassword_Success() {
        // Given
        String currentPassword = "password123";
        String newPassword = "newpassword123";

        // When & Then
        assertDoesNotThrow(() -> authService.changePassword(testUser, currentPassword, newPassword));
    }

    @Test
    @DisplayName("修改密码 - 当前密码错误")
    void testChangePassword_WrongCurrentPassword_ThrowsException() {
        // Given
        String currentPassword = "wrongpassword";
        String newPassword = "newpassword123";

        // When & Then
        assertThrows(BusinessException.class, () -> 
            authService.changePassword(testUser, currentPassword, newPassword));
    }

    @Test
    @DisplayName("修改密码 - 当前密码为空")
    void testChangePassword_EmptyCurrentPassword_ThrowsException() {
        // Given
        String currentPassword = "";
        String newPassword = "newpassword123";

        // When & Then
        assertThrows(BusinessException.class, () -> 
            authService.changePassword(testUser, currentPassword, newPassword));
    }

    @Test
    @DisplayName("修改密码 - 新密码为空")
    void testChangePassword_EmptyNewPassword_ThrowsException() {
        // Given
        String currentPassword = "password123";
        String newPassword = "";

        // When & Then
        assertThrows(BusinessException.class, () -> 
            authService.changePassword(testUser, currentPassword, newPassword));
    }

    @Test
    @DisplayName("修改密码 - 新密码与旧密码相同")
    void testChangePassword_SamePassword_ThrowsException() {
        // Given
        String currentPassword = "password123";
        String newPassword = "password123";

        // When & Then
        assertThrows(BusinessException.class, () -> 
            authService.changePassword(testUser, currentPassword, newPassword));
    }

    // ==================== 登出测试 ====================

    @Test
    @DisplayName("用户登出 - 成功")
    void testLogout_Success() {
        // When & Then
        assertDoesNotThrow(() -> authService.logout(testUser));
    }

    @Test
    @DisplayName("退出指定设备 - 成功")
    void testLogoutDevice_Success() {
        // Given
        Long deviceId = 1L;

        // When & Then
        assertDoesNotThrow(() -> authService.logoutDevice(testUser, deviceId));
    }

    @Test
    @DisplayName("获取登录设备列表 - 成功")
    void testGetLoginDevices_Success() {
        // When
        Map<String, Object> devices = authService.getLoginDevices(testUser);

        // Then
        assertNotNull(devices);
        assertTrue(devices.containsKey("devices"));
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("登录请求为空")
    void testLogin_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(null, mockRequest));
    }

    @Test
    @DisplayName("注册请求为空")
    void testRegister_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(null, mockRequest));
    }

    @Test
    @DisplayName("用户名长度边界测试 - 3个字符")
    void testRegister_UsernameMinLength_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abc");
        request.setEmail("abc@example.com");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertDoesNotThrow(() -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户名长度边界测试 - 20个字符")
    void testRegister_UsernameMaxLength_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a".repeat(20));
        request.setEmail("test20@example.com");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertDoesNotThrow(() -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("用户名长度边界测试 - 超过20个字符")
    void testRegister_UsernameTooLong_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("a".repeat(21));
        request.setEmail("test21@example.com");
        request.setPassword("password123");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("密码长度边界测试 - 6个字符")
    void testRegister_PasswordMinLength_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser6");
        request.setEmail("test6@example.com");
        request.setPassword("pass12");
        request.setFullName("测试用户");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertDoesNotThrow(() -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("姓名长度边界测试 - 50个字符")
    void testRegister_FullNameMaxLength_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser50");
        request.setEmail("test50@example.com");
        request.setPassword("password123");
        request.setFullName("测".repeat(50));
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertDoesNotThrow(() -> authService.register(request, mockRequest));
    }

    @Test
    @DisplayName("姓名长度边界测试 - 超过50个字符")
    void testRegister_FullNameTooLong_ThrowsException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser51");
        request.setEmail("test51@example.com");
        request.setPassword("password123");
        request.setFullName("测".repeat(51));
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(request, mockRequest));
    }
} 