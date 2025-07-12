
package com.aicourse.service;

import com.aicourse.dto.LoginRequest;
import com.aicourse.dto.RegisterRequest;
import com.aicourse.dto.TokenResponse;
import com.aicourse.dto.UserResponse;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.repository.UserRepository;
import com.aicourse.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService authService;

    private User studentUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        studentUser = new User();
        studentUser.setId(1L);
        studentUser.setUsername("testuser");
        studentUser.setEmail("test@example.com");
        studentUser.setFullName("Test User");
        studentUser.setHashedPassword("hashedPassword");
        studentUser.setRole(UserRole.STUDENT);
        studentUser.setIsActive(true);
        studentUser.setCreatedAt(LocalDateTime.now());

        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setUsername("inactiveuser");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setHashedPassword("hashedPassword");
        inactiveUser.setRole(UserRole.STUDENT);
        inactiveUser.setIsActive(false);
    }

    // =================== Login Tests ===================

    @Test
    @DisplayName("login - Success")
    void login_success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateToken(anyLong(), anyString())).thenReturn("test_token");
        when(jwtUtils.getTokenRemainingTime(anyString())).thenReturn(3600L);

        TokenResponse response = authService.login(loginRequest, httpServletRequest);

        assertNotNull(response);
        assertEquals("test_token", response.getTokenType());
        verify(userRepository, times(1)).save(studentUser);
        verify(userSessionService, times(1)).createUserSessionAsync(any(User.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("loginWithUserInfo - Success")
    void loginWithUserInfo_success() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtils.generateToken(anyLong(), anyString())).thenReturn("test_token");

        Map<String, Object> response = authService.loginWithUserInfo(loginRequest, httpServletRequest);

        assertNotNull(response);
        assertEquals("test_token", response.get("token"));
        assertTrue(response.containsKey("user"));
        UserResponse userResponse = (UserResponse) response.get("user");
        assertEquals("testuser", userResponse.getUsername());
    }

    @Test
    @DisplayName("login - User Not Found")
    void login_userNotFound() {
        LoginRequest loginRequest = new LoginRequest("unknownuser", "password123");
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknownuser")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest, httpServletRequest);
        });
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("login - Incorrect Password")
    void login_incorrectPassword() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest, httpServletRequest);
        });
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("login - Inactive User")
    void login_inactiveUser() {
        LoginRequest loginRequest = new LoginRequest("inactiveuser", "password123");
        when(userRepository.findByUsername("inactiveuser")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest, httpServletRequest);
        });
        assertEquals("用户账户已被禁用，请联系管理员", exception.getMessage());
    }

    @Test
    @DisplayName("login - Invalid Request")
    void login_invalidRequest() {
        assertThrows(BusinessException.class, () -> authService.login(new LoginRequest(null, "p"), httpServletRequest));
        assertThrows(BusinessException.class, () -> authService.login(new LoginRequest("u", null), httpServletRequest));
        assertThrows(BusinessException.class, () -> authService.login(new LoginRequest("u", "short"), httpServletRequest));
    }

    // =================== Register Tests ===================

    @Test
    @DisplayName("register - Success")
    void register_success() {
        RegisterRequest registerRequest = new RegisterRequest("newuser", "new@example.com", "password123", "New User", UserRole.STUDENT);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(registerRequest, httpServletRequest);

        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("new@example.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register - Username Exists")
    void register_usernameExists() {
        RegisterRequest registerRequest = new RegisterRequest("testuser", "new@example.com", "password123", "New User", UserRole.STUDENT);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest, httpServletRequest);
        });
        assertEquals("用户名已存在，请选择其他用户名", exception.getMessage());
    }

    @Test
    @DisplayName("register - Email Exists")
    void register_emailExists() {
        RegisterRequest registerRequest = new RegisterRequest("newuser", "test@example.com", "password123", "New User", UserRole.STUDENT);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest, httpServletRequest);
        });
        assertEquals("邮箱已被注册，请使用其他邮箱或直接登录", exception.getMessage());
    }

    @Test
    @DisplayName("register - Invalid Role (Admin)")
    void register_invalidRoleAdmin() {
        RegisterRequest registerRequest = new RegisterRequest("newadmin", "admin@example.com", "password123", "New Admin", UserRole.ADMIN);
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest, httpServletRequest);
        });
        assertEquals("不允许注册管理员账户", exception.getMessage());
    }

    @Test
    @DisplayName("register - Invalid Request Fields")
    void register_invalidRequestFields() {
        // Invalid username
        RegisterRequest req1 = new RegisterRequest("a", "e@e.com", "p123456", "f", UserRole.STUDENT);
        assertThrows(BusinessException.class, () -> authService.register(req1, httpServletRequest));

        // Invalid email
        RegisterRequest req2 = new RegisterRequest("validuser", "invalid-email", "p123456", "f", UserRole.STUDENT);
        assertThrows(BusinessException.class, () -> authService.register(req2, httpServletRequest));

        // Invalid password
        RegisterRequest req3 = new RegisterRequest("validuser", "e@e.com", "short", "f", UserRole.STUDENT);
        assertThrows(BusinessException.class, () -> authService.register(req3, httpServletRequest));
    }

    // =================== Update User Info Tests ===================

    @Test
    @DisplayName("updateUserInfo - Success")
    void updateUserInfo_success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", "Updated Name");
        updates.put("bio", "This is my new bio.");

        when(userRepository.save(any(User.class))).thenReturn(studentUser);

        UserResponse response = authService.updateUserInfo(studentUser, updates);

        assertNotNull(response);
        assertEquals("Updated Name", response.getFullName());
        assertEquals("This is my new bio.", studentUser.getBio());
        verify(userRepository, times(1)).save(studentUser);
    }

    @Test
    @DisplayName("updateUserInfo - Forbidden Field")
    void updateUserInfo_forbiddenField() {
        Map<String, Object> updates = Collections.singletonMap("username", "newusername");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.updateUserInfo(studentUser, updates);
        });
        assertEquals("不允许更新字段: username", exception.getMessage());
    }

    @Test
    @DisplayName("updateUserInfo - Invalid Field Value")
    void updateUserInfo_invalidFieldValue() {
        Map<String, Object> updates = Collections.singletonMap("fullName", "a".repeat(51)); // Exceeds max length

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.updateUserInfo(studentUser, updates);
        });
        assertEquals("姓名长度不能超过50个字符", exception.getMessage());
    }

    // =================== Change Password Tests ===================

    @Test
    @DisplayName("changePassword - Success")
    void changePassword_success() {
        when(passwordEncoder.matches("hashedPassword", studentUser.getHashedPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

        authService.changePassword(studentUser, "hashedPassword", "newPassword123");

        assertEquals("newHashedPassword", studentUser.getHashedPassword());
        verify(userRepository, times(1)).save(studentUser);
        verify(userSessionService, times(1)).logoutAllDevices(studentUser);
    }

    @Test
    @DisplayName("changePassword - Incorrect Current Password")
    void changePassword_incorrectCurrentPassword() {
        when(passwordEncoder.matches("wrongPassword", studentUser.getHashedPassword())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.changePassword(studentUser, "wrongPassword", "newPassword123");
        });
        assertEquals("当前密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("changePassword - New Password Same as Old")
    void changePassword_newPasswordSameAsOld() {
        when(passwordEncoder.matches("hashedPassword", studentUser.getHashedPassword())).thenReturn(true);
        when(passwordEncoder.matches("hashedPassword", studentUser.getHashedPassword())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.changePassword(studentUser, "hashedPassword", "hashedPassword");
        });
        assertEquals("新密码不能与当前密码相同", exception.getMessage());
    }

    @Test
    @DisplayName("changePassword - Invalid New Password")
    void changePassword_invalidNewPassword() {
        when(passwordEncoder.matches("hashedPassword", studentUser.getHashedPassword())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.changePassword(studentUser, "hashedPassword", "short");
        });
        assertEquals("新密码长度不能少于6个字符", exception.getMessage());
    }

    // =================== Other Methods Tests ===================

    @Test
    @DisplayName("getCurrentUserInfo - Success")
    void getCurrentUserInfo_success() {
        UserResponse response = authService.getCurrentUserInfo(studentUser);
        assertEquals(studentUser.getUsername(), response.getUsername());
        assertEquals(studentUser.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("logout - Success")
    void logout_success() {
        authService.logout(studentUser);
        verify(userSessionService, times(1)).logoutAllDevices(studentUser);
    }

    @Test
    @DisplayName("logoutDevice - Success")
    void logoutDevice_success() {
        authService.logoutDevice(studentUser, 123L);
        verify(userSessionService, times(1)).logoutDevice(studentUser, 123L);
    }

    @Test
    @DisplayName("getLoginDevices - Success")
    void getLoginDevices_success() {
        Map<String, Object> mockDevices = new HashMap<>();
        mockDevices.put("devices", Collections.singletonList("device1"));
        when(userSessionService.getLoginDevices(studentUser)).thenReturn(mockDevices);

        Map<String, Object> devices = authService.getLoginDevices(studentUser);

        assertNotNull(devices);
        assertTrue(devices.containsKey("devices"));
        verify(userSessionService, times(1)).getLoginDevices(studentUser);
    }
}
