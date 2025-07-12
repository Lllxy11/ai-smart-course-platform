package com.aicourse.service;

import com.aicourse.entity.Notification;
import com.aicourse.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 单元测试")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    // Use @Spy to allow real method calls on ObjectMapper, unless we want to mock it.
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationService notificationService;

    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // To ensure the rateLimitCache is clean for each test, we can re-initialize the service
        // or clear the cache. Re-initializing is safer if the constructor does more work.
        // For this case, we can use reflection to reset the cache.
        Map<Long, Map<String, LocalDateTime>> rateLimitCache = 
            (Map<Long, Map<String, LocalDateTime>>) ReflectionTestUtils.getField(notificationService, "rateLimitCache");
        if (rateLimitCache != null) {
            rateLimitCache.clear();
        }
    }

    @Nested
    @DisplayName("sendNotification 方法测试")
    class SendNotificationTests {

        @Test
        @DisplayName("成功发送通知")
        void sendNotification_success() throws JsonProcessingException {
            // Arrange
            String templateId = "task_deadline";
            Map<String, Object> customData = Map.of("taskName", "Final Project");
            Notification savedNotification = new Notification();
            savedNotification.setId(100L);
            savedNotification.setUserId(testUserId);
            savedNotification.setTitle("任务截止提醒");
            savedNotification.setContent("您有任务 Final Project 即将截止，请及时完成"); // Content should be formatted

            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
            
            // Act
            NotificationService.NotificationData result = notificationService.sendNotification(testUserId, templateId, customData, "/tasks/1");

            // Assert
            assertNotNull(result);
            assertEquals(String.valueOf(100L), result.getId());
            assertEquals("任务截止提醒", result.getTitle());
            assertTrue(result.getContent().contains("Final Project"));
            assertEquals("/tasks/1", result.getActionUrl());

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertEquals(objectMapper.writeValueAsString(customData), captor.getValue().getData());
        }

        @Test
        @DisplayName("模板不存在时应返回 null")
        void sendNotification_templateNotFound_shouldReturnNull() {
            // Act
            NotificationService.NotificationData result = notificationService.sendNotification(testUserId, "non_existent_template", null, null);
            
            // Assert
            assertNull(result);
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("触发频率限制时应返回 null")
        void sendNotification_rateLimited_shouldReturnNull() {
            // Arrange
            String templateId = "idle_reminder";
            when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

            // Act
            // First call should succeed
            NotificationService.NotificationData firstResult = notificationService.sendNotification(testUserId, templateId, null, null);
            // Second call immediately after should fail
            NotificationService.NotificationData secondResult = notificationService.sendNotification(testUserId, templateId, null, null);

            // Assert
            assertNotNull(firstResult);
            assertNull(secondResult);
            // Verify save was only called once
            verify(notificationRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("保存到数据库失败时应返回 null")
        void sendNotification_saveFails_shouldReturnNull() {
            // Arrange
            when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("DB connection failed"));
            
            // Act
            NotificationService.NotificationData result = notificationService.sendNotification(testUserId, "task_deadline", null, null);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getUserNotifications 方法测试")
    class GetUserNotificationsTests {

        private Notification createMockNotification(Long id, String category, boolean isRead) {
            Notification n = new Notification();
            n.setId(id);
            n.setUserId(testUserId);
            n.setCategory(category);
            n.setIsRead(isRead);
            n.setCreatedAt(LocalDateTime.now());
            n.setTitle("Test");
            n.setContent("Test Content");
            return n;
        }

        @Test
        @DisplayName("获取所有通知")
        void getUserNotifications_all() {
            // Arrange
            List<Notification> mockList = List.of(createMockNotification(1L, "learning", false));
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(testUserId), any(PageRequest.class))).thenReturn(mockList);

            // Act
            List<NotificationService.NotificationData> result = notificationService.getUserNotifications(testUserId, 10, false, null);

            // Assert
            assertEquals(1, result.size());
            verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(eq(testUserId), any(PageRequest.class));
        }

        @Test
        @DisplayName("只获取未读通知")
        void getUserNotifications_unreadOnly() {
            // Arrange
            List<Notification> mockList = List.of(createMockNotification(1L, "learning", false));
            when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(eq(testUserId), eq(false), any(PageRequest.class))).thenReturn(mockList);

            // Act
            List<NotificationService.NotificationData> result = notificationService.getUserNotifications(testUserId, 10, true, null);

            // Assert
            assertEquals(1, result.size());
            verify(notificationRepository).findByUserIdAndIsReadOrderByCreatedAtDesc(eq(testUserId), eq(false), any(PageRequest.class));
        }

        @Test
        @DisplayName("按分类获取未读通知")
        void getUserNotifications_unreadAndByCategory() {
            // Arrange
            String category = "system";
            List<Notification> mockList = List.of(createMockNotification(1L, category, false));
            when(notificationRepository.findByUserIdAndCategoryAndIsReadOrderByCreatedAtDesc(eq(testUserId), eq(category), eq(false), any(PageRequest.class))).thenReturn(mockList);

            // Act
            List<NotificationService.NotificationData> result = notificationService.getUserNotifications(testUserId, 10, true, category);

            // Assert
            assertEquals(1, result.size());
            verify(notificationRepository).findByUserIdAndCategoryAndIsReadOrderByCreatedAtDesc(eq(testUserId), eq(category), eq(false), any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("更新和删除操作测试")
    class UpdateAndDeleteTests {

        @Test
        @DisplayName("成功标记为已读")
        void markAsRead_success() {
            // Arrange
            Notification notification = new Notification();
            notification.setIsRead(false);
            when(notificationRepository.findByIdAndUserId(1L, testUserId)).thenReturn(Optional.of(notification));
            
            // Act
            boolean result = notificationService.markAsRead(1L, testUserId);
            
            // Assert
            assertTrue(result);
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertTrue(captor.getValue().getIsRead());
            assertNotNull(captor.getValue().getReadAt());
        }

        @Test
        @DisplayName("标记为已读失败（通知不存在）")
        void markAsRead_notFound_shouldFail() {
            // Arrange
            when(notificationRepository.findByIdAndUserId(1L, testUserId)).thenReturn(Optional.empty());
            
            // Act
            boolean result = notificationService.markAsRead(1L, testUserId);

            // Assert
            assertFalse(result);
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("成功删除通知")
        void deleteNotification_success() {
            // Arrange
            when(notificationRepository.deleteByIdAndUserId(1L, testUserId)).thenReturn(1);
            
            // Act
            boolean result = notificationService.deleteNotification(1L, testUserId);
            
            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("删除通知失败（通知不存在）")
        void deleteNotification_notFound_shouldFail() {
            // Arrange
            when(notificationRepository.deleteByIdAndUserId(1L, testUserId)).thenReturn(0);
            
            // Act
            boolean result = notificationService.deleteNotification(1L, testUserId);
            
            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("标记所有为已读")
        void markAllAsRead_all() {
            // Arrange
            when(notificationRepository.markAllAsReadByUserId(eq(testUserId), any(LocalDateTime.class))).thenReturn(5);

            // Act
            int count = notificationService.markAllAsRead(testUserId, null);
            
            // Assert
            assertEquals(5, count);
        }
    }

    @Nested
    @DisplayName("摘要和批量操作测试")
    class SummaryAndBatchTests {

        @Test
        @DisplayName("获取通知摘要")
        void getNotificationSummary_success() {
            // Arrange
            when(notificationRepository.countByUserIdAndCategoryAndIsRead(testUserId, "learning", false)).thenReturn(2L);
            when(notificationRepository.countByUserIdAndCategoryAndIsRead(testUserId, "system", false)).thenReturn(1L);
            // Mock the call made by getUserNotifications inside getNotificationSummary
            when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(eq(testUserId), eq(false), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

            // Act
            Map<String, Object> summary = notificationService.getNotificationSummary(testUserId);
            
            // Assert
            assertEquals(3L, summary.get("total_unread"));
            Map<String, Long> unreadByCategory = (Map<String, Long>) summary.get("unread_by_category");
            assertEquals(2L, unreadByCategory.get("learning"));
            assertEquals(1L, unreadByCategory.get("system"));
        }

        @Test
        @DisplayName("批量发送通知")
        void sendBatchNotification_success() {
            // This test demonstrates a simpler way without spying, by controlling the mocked repository
            // Arrange
            List<Long> userIds = List.of(1L, 2L, 3L);
            when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

            // Act
            int successCount = notificationService.sendBatchNotification(userIds, "course_updated", null);

            // Assert
            assertEquals(3, successCount);
            verify(notificationRepository, times(3)).save(any());
        }
    }

    @Nested
    @DisplayName("业务场景方法测试")
    class BusinessScenarioTests {

        @Test
        @DisplayName("创建成就通知应调用 sendNotification")
        void createAchievementNotification_shouldCallSendNotification() {
            // Use a spy to verify the internal method call
            NotificationService spyService = spy(notificationService);
            // We must stub the spied method to avoid its real execution
            doReturn(null).when(spyService).sendNotification(anyLong(), anyString(), any(), any());

            // Act
            spyService.createAchievementNotification(testUserId, "level_up", Map.of("level", 5));
            
            // Assert
            ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);
            verify(spyService).sendNotification(eq(testUserId), templateCaptor.capture(), dataCaptor.capture(), eq("/profile"));
            
            assertEquals("level_up", templateCaptor.getValue());
            assertEquals(5, dataCaptor.getValue().get("level"));
        }
    }
}