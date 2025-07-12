package com.aicourse.service;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VideoLearningServiceTest {

    @InjectMocks
    private VideoLearningService videoLearningService;

    private User studentUser, anotherStudentUser, teacherUser, adminUser;
    private final Long taskId = 1L;
    private final Long courseId = 1L;

    @BeforeEach
    void setUp() {
        studentUser = new User();
        studentUser.setId(1L);
        studentUser.setRole(UserRole.STUDENT);

        anotherStudentUser = new User();
        anotherStudentUser.setId(2L);
        anotherStudentUser.setRole(UserRole.STUDENT);

        teacherUser = new User();
        teacherUser.setId(10L);
        teacherUser.setRole(UserRole.TEACHER);

        adminUser = new User();
        adminUser.setId(100L);
        adminUser.setRole(UserRole.ADMIN);
    }

    @Test
    @DisplayName("记录视频事件 - 成功记录")
    void recordVideoEvent_Success() {
        // Arrange
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_type", "play");

        // Act
        Map<String, Object> result = videoLearningService.recordVideoEvent(eventData, studentUser);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("事件记录成功", result.get("message"));
        assertEquals("play", result.get("event_type"));
    }
    
    @Test
    @DisplayName("记录视频事件 - 发生异常时返回失败")
    void recordVideoEvent_HandlesException() {
        // Arrange
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_type", 123); // 错误的数据类型，将导致 ClassCastException

        // Act
        Map<String, Object> result = videoLearningService.recordVideoEvent(eventData, studentUser);
        
        // Assert
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(Objects.requireNonNull(result.get("message")).toString().contains("记录视频事件失败"));
    }

    @Test
    @DisplayName("获取视频分析 - 学生查看自己的数据")
    void getVideoAnalytics_StudentViewsOwnData_Success() {
        // Act
        Map<String, Object> result = videoLearningService.getVideoAnalytics(taskId, studentUser.getId(), studentUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("summary"));
        assertTrue(result.containsKey("completion_distribution"));
    }

    @Test
    @DisplayName("获取视频分析 - 教师查看学生数据")
    void getVideoAnalytics_TeacherViewsStudentData_Success() {
        // Act
        Map<String, Object> result = videoLearningService.getVideoAnalytics(taskId, studentUser.getId(), teacherUser);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("获取视频分析 - 学生查看他人数据应抛出异常")
    void getVideoAnalytics_StudentViewsOtherData_ShouldThrowException() {
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoLearningService.getVideoAnalytics(taskId, anotherStudentUser.getId(), studentUser);
        });
        assertEquals("只能查看自己的学习数据", exception.getMessage());
    }
    
    @Test
    @DisplayName("获取视频热力图 - 学生查看自己的数据")
    void getVideoHeatmap_StudentViewsOwnData_Success() {
        // Act
        Map<String, Object> result = videoLearningService.getVideoHeatmap(taskId, studentUser.getId(), studentUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("heatmap_data"));
    }

    @Test
    @DisplayName("获取视频热力图 - 学生查看他人数据应抛出异常")
    void getVideoHeatmap_StudentViewsOtherData_ShouldThrowException() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            videoLearningService.getVideoHeatmap(taskId, anotherStudentUser.getId(), studentUser);
        });
    }

    @Test
    @DisplayName("获取学习质量报告 - 学生查看自己的数据")
    void getLearningQualityReport_StudentViewsOwnData_Success() {
        // Act
        Map<String, Object> result = videoLearningService.getLearningQualityReport(taskId, studentUser.getId(), studentUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("overall_quality"));
        assertTrue(result.containsKey("identified_issues"));
    }

    @Test
    @DisplayName("获取学习质量报告 - 学生查看他人数据应抛出异常")
    void getLearningQualityReport_StudentViewsOtherData_ShouldThrowException() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            videoLearningService.getLearningQualityReport(taskId, anotherStudentUser.getId(), studentUser);
        });
    }

    @Test
    @DisplayName("获取视频学习进度 - 学生查看自己的进度")
    void getVideoLearningProgress_StudentViewsOwnProgress_Success() {
        // Act
        Map<String, Object> result = videoLearningService.getVideoLearningProgress(studentUser.getId(), studentUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("total_videos"));
        assertTrue(result.containsKey("recent_videos"));
    }


    @Test
    @DisplayName("获取视频学习进度 - 学生查看他人进度应抛出异常")
    void getVideoLearningProgress_StudentViewsOtherProgress_ShouldThrowException() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            videoLearningService.getVideoLearningProgress(anotherStudentUser.getId(), studentUser);
        });
    }

    @Test
    @DisplayName("获取视频学习统计 - 教师获取统计数据")
    void getVideoLearningStatistics_TeacherGetsStats_Success() {
        // Act
        Map<String, Object> result = videoLearningService.getVideoLearningStatistics(courseId, taskId, teacherUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("total_video_records"));
        assertTrue(result.containsKey("popular_videos"));
    }

    @Test
    @DisplayName("获取视频学习统计 - 管理员获取统计数据")
    void getVideoLearningStatistics_AdminGetsStats_Success() {
        // Act
        Map<String, Object> result = videoLearningService.getVideoLearningStatistics(courseId, taskId, adminUser);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("获取视频学习统计 - 学生获取统计数据应抛出异常")
    void getVideoLearningStatistics_StudentGetsStats_ShouldThrowException() {
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoLearningService.getVideoLearningStatistics(courseId, taskId, studentUser);
        });
        assertEquals("权限不足", exception.getMessage());
    }
}