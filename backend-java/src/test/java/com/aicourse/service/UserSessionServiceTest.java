package com.aicourse.service;

import com.aicourse.entity.User;
import com.aicourse.entity.UserSession;
import com.aicourse.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private HttpServletRequest request;

    // 使用 @Spy 来监视我们自己的服务，以便测试异步方法
    @Spy
    @InjectMocks
    private UserSessionService userSessionService;

    private User user;
    private final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private final String TEST_IP_ADDRESS = "192.168.1.100";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        // 为 request mock 设置通用行为
        when(request.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
        when(request.getHeader("X-Forwarded-For")).thenReturn(TEST_IP_ADDRESS);
    }

    @Test
    @DisplayName("创建用户会话 - 成功路径")
    void createUserSession_Success() {
        // Arrange
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            session.setId(100L); // 模拟数据库生成ID
            return session;
        });

        // Act
        UserSession session = userSessionService.createUserSession(user, request);

        // Assert
        assertNotNull(session);
        assertEquals(user.getId(), session.getUserId());
        assertTrue(session.getIsCurrent());
        assertTrue(session.getIsActive());

        // 验证其他会话被设为非当前
        verify(userSessionRepository, times(1)).setOtherSessionsAsNotCurrent(user.getId());

        // 使用 ArgumentCaptor 捕获被保存的实体
        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(userSessionRepository, times(1)).save(sessionCaptor.capture());

        UserSession capturedSession = sessionCaptor.getValue();
        assertEquals("Chrome on Windows 10", capturedSession.getDeviceName());
        assertTrue(capturedSession.getBrowser().startsWith("Chrome"));
        assertEquals("Windows 10", capturedSession.getOperatingSystem());
        assertEquals(TEST_IP_ADDRESS, capturedSession.getIpAddress());
        assertNotNull(capturedSession.getSessionToken());
    }

    @Test
    @DisplayName("创建用户会话 - 当仓库保存失败时应返回null")
    void createUserSession_WhenRepositoryFails_ShouldReturnNull() {
        // Arrange
        // 模拟数据库保存时抛出异常
        when(userSessionRepository.save(any(UserSession.class))).thenThrow(new RuntimeException("Database connection failed"));

        // Act
        UserSession session = userSessionService.createUserSession(user, request);

        // Assert
        assertNull(session, "在发生异常时，方法应返回null而不抛出异常");
        // 验证仍然尝试了设置其他会话
        verify(userSessionRepository, times(1)).setOtherSessionsAsNotCurrent(user.getId());
    }

    @Test
    @DisplayName("异步创建用户会话 - 应该调用同步方法")
    void createUserSessionAsync_ShouldDelegateToSyncMethod() throws InterruptedException {
        // Arrange
        // 因为我们使用了 @Spy，我们可以监视对我们自己服务方法的调用
        // 我们需要模拟同步方法的行为，以避免它实际执行
        doReturn(new UserSession()).when(userSessionService).createUserSession(any(User.class), any(HttpServletRequest.class));

        // Act
        userSessionService.createUserSessionAsync(user, request);

        // Assert
        // 等待一小段时间，让新线程有机会执行
        // 注意：这在CI/CD环境中可能不稳定，但对于测试这种特定模式是务实的
        Thread.sleep(200);

        // 验证同步方法被调用了一次
        verify(userSessionService, times(1)).createUserSession(user, request);
    }

    @Test
    @DisplayName("获取登录设备 - 返回正确的设备列表")
    void getLoginDevices_ReturnsCorrectDeviceList() {
        // Arrange
        UserSession session1 = new UserSession();
        session1.setId(1L);
        session1.setDeviceName("Chrome on Windows");
        session1.setIpAddress("1.1.1.1");
        session1.setIsCurrent(true);

        when(userSessionRepository.findByUserIdAndIsActive(user.getId(), true)).thenReturn(Collections.singletonList(session1));

        // Act
        Map<String, Object> result = userSessionService.getLoginDevices(user);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("devices"));
        List<Map<String, Object>> devices = (List<Map<String, Object>>) result.get("devices");
        assertEquals(1, devices.size());
        assertEquals(1L, devices.get(0).get("id"));
        assertEquals("Chrome on Windows", devices.get(0).get("device_name"));
        assertTrue((Boolean) devices.get(0).get("is_current"));
    }

    @Test
    @DisplayName("退出指定设备 - 正确调用仓库方法")
    void logoutDevice_CallsRepositoryCorrectly() {
        // Arrange
        Long deviceId = 123L;

        // Act
        userSessionService.logoutDevice(user, deviceId);

        // Assert
        // 验证仓库的deactivateSession方法被以正确的参数调用
        verify(userSessionRepository, times(1)).deactivateSession(deviceId, user.getId());
    }

    @Test
    @DisplayName("退出所有设备 - 正确调用仓库方法")
    void logoutAllDevices_CallsRepositoryCorrectly() {
        // Act
        userSessionService.logoutAllDevices(user);

        // Assert
        // 验证仓库的deactivateAllUserSessions方法被以正确的用户ID调用
        verify(userSessionRepository, times(1)).deactivateAllUserSessions(user.getId());
    }
}