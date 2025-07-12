package com.aicourse.service;

import com.aicourse.entity.LearningRecord;
import com.aicourse.entity.KnowledgePoint;
import com.aicourse.entity.User;
import com.aicourse.repository.LearningRecordRepository;
import com.aicourse.repository.KnowledgePointRepository;
import com.aicourse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearningRecordService 单元测试")
class LearningRecordServiceTest {

    @Mock
    private LearningRecordRepository learningRecordRepository;

    @Mock
    private KnowledgePointRepository knowledgePointRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LearningRecordService learningRecordService;

    private User currentUser;
    private User studentUser;
    private KnowledgePoint knowledgePoint;
    private Long studentId = 1L;
    private Long knowledgePointId = 101L;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(studentId);
        currentUser.setUsername("test-student");

        studentUser = new User();
        studentUser.setId(studentId);
        studentUser.setUsername("test-student");
        
        knowledgePoint = new KnowledgePoint();
        knowledgePoint.setId(knowledgePointId);
        knowledgePoint.setName("Java 基础");
    }

    @Nested
    @DisplayName("createLearningRecord 方法测试")
    class CreateLearningRecordTests {

        @Test
        @DisplayName("成功创建学习记录")
        void createLearningRecord_success() {
            // Arrange
            Map<String, Object> recordData = new HashMap<>();
            recordData.put("knowledgePointId", knowledgePointId);
            recordData.put("studentId", studentId);
            recordData.put("actionType", "PRACTICE");
            recordData.put("duration", 30);
            recordData.put("completed", true);
            recordData.put("score", 95);

            when(knowledgePointRepository.findById(knowledgePointId)).thenReturn(Optional.of(knowledgePoint));
            when(userRepository.findById(studentId)).thenReturn(Optional.of(studentUser));
            
            LearningRecord savedRecord = new LearningRecord();
            savedRecord.setId(1L);
            savedRecord.setKnowledgePoint(knowledgePoint);
            savedRecord.setStudent(studentUser);
            savedRecord.setActionType(LearningRecord.ActionType.PRACTICE);
            savedRecord.setDuration(30);
            savedRecord.setCompleted(true);
            savedRecord.setScore(95);
            savedRecord.setLearningTime(LocalDateTime.now());
            when(learningRecordRepository.save(any(LearningRecord.class))).thenReturn(savedRecord);

            // Act
            Map<String, Object> result = learningRecordService.createLearningRecord(recordData, currentUser);

            // Assert
            assertTrue((Boolean) result.get("success"));
            assertEquals("学习记录创建成功", result.get("message"));
            assertNotNull(result.get("record"));

            ArgumentCaptor<LearningRecord> captor = ArgumentCaptor.forClass(LearningRecord.class);
            verify(learningRecordRepository).save(captor.capture());

            LearningRecord capturedRecord = captor.getValue();
            assertEquals(knowledgePoint, capturedRecord.getKnowledgePoint());
            assertEquals(studentUser, capturedRecord.getStudent());
            assertEquals(LearningRecord.ActionType.PRACTICE, capturedRecord.getActionType());
            assertEquals(95, capturedRecord.getScore());
            assertTrue(capturedRecord.isCompleted());
        }

        @Test
        @DisplayName("知识点不存在时创建失败")
        void createLearningRecord_knowledgePointNotFound_shouldFail() {
            // Arrange
            Map<String, Object> recordData = new HashMap<>();
            recordData.put("knowledgePointId", 999L);
            when(knowledgePointRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Map<String, Object> result = learningRecordService.createLearningRecord(recordData, currentUser);
            
            // Assert
            assertFalse((Boolean) result.get("success"));
            assertEquals("知识点不存在", result.get("message"));
            verify(learningRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("ActionType 无效时使用默认值 VIEW")
        void createLearningRecord_invalidActionType_shouldUseDefault() {
            // Arrange
            Map<String, Object> recordData = new HashMap<>();
            recordData.put("knowledgePointId", knowledgePointId);
            recordData.put("actionType", "INVALID_TYPE");
            
            when(knowledgePointRepository.findById(knowledgePointId)).thenReturn(Optional.of(knowledgePoint));
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(studentUser));
            when(learningRecordRepository.save(any(LearningRecord.class))).thenReturn(new LearningRecord());

            // Act
            learningRecordService.createLearningRecord(recordData, currentUser);

            // Assert
            ArgumentCaptor<LearningRecord> captor = ArgumentCaptor.forClass(LearningRecord.class);
            verify(learningRecordRepository).save(captor.capture());
            assertEquals(LearningRecord.ActionType.VIEW, captor.getValue().getActionType());
        }
    }

    @Nested
    @DisplayName("getStudentLearningRecords 方法测试")
    class GetStudentLearningRecordsTests {

        private LearningRecord createMockRecord(Long id, LearningRecord.ActionType type) {
            LearningRecord record = new LearningRecord();
            record.setId(id);
            record.setStudent(studentUser);
            record.setKnowledgePoint(knowledgePoint);
            record.setActionType(type);
            record.setLearningTime(LocalDateTime.now());
            return record;
        }

        @Test
        @DisplayName("无筛选条件时获取所有记录")
        void getStudentLearningRecords_noFilters_shouldReturnAll() {
            // Arrange
            List<LearningRecord> mockRecords = Arrays.asList(
                createMockRecord(1L, LearningRecord.ActionType.VIEW),
                createMockRecord(2L, LearningRecord.ActionType.PRACTICE)
            );
            when(learningRecordRepository.findByStudentId(studentId)).thenReturn(mockRecords);
            
            // Act
            Map<String, Object> result = learningRecordService.getStudentLearningRecords(studentId, null, null, 0, 10, currentUser);
            
            // Assert
            assertTrue((Boolean) result.get("success"));
            assertEquals(2, ((List<?>) result.get("records")).size());
            assertEquals(2, result.get("total"));
            verify(learningRecordRepository).findByStudentId(studentId);
            verify(learningRecordRepository, never()).findByStudentIdAndKnowledgePointId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("根据知识点和行为类型筛选")
        void getStudentLearningRecords_withAllFilters_shouldReturnFiltered() {
            // Arrange
            List<LearningRecord> mockRecords = List.of(createMockRecord(1L, LearningRecord.ActionType.PRACTICE));
            when(learningRecordRepository.findByStudentIdAndKnowledgePointIdAndActionType(studentId, knowledgePointId, LearningRecord.ActionType.PRACTICE))
                .thenReturn(mockRecords);
            
            // Act
            Map<String, Object> result = learningRecordService.getStudentLearningRecords(studentId, knowledgePointId, "PRACTICE", 0, 10, currentUser);
            
            // Assert
            assertTrue((Boolean) result.get("success"));
            assertEquals(1, ((List<?>) result.get("records")).size());
            verify(learningRecordRepository).findByStudentIdAndKnowledgePointIdAndActionType(anyLong(), anyLong(), any());
        }

        @Test
        @DisplayName("测试分页逻辑")
        void getStudentLearningRecords_shouldPaginateCorrectly() {
            // Arrange
            List<LearningRecord> mockRecords = Arrays.asList(
                createMockRecord(1L, LearningRecord.ActionType.VIEW),
                createMockRecord(2L, LearningRecord.ActionType.VIEW),
                createMockRecord(3L, LearningRecord.ActionType.VIEW)
            );
            when(learningRecordRepository.findByStudentId(studentId)).thenReturn(mockRecords);

            // Act
            Map<String, Object> result = learningRecordService.getStudentLearningRecords(studentId, null, null, 1, 1, currentUser);
            
            // Assert
            List<Map<String, Object>> records = (List<Map<String, Object>>) result.get("records");
            assertEquals(1, records.size());
            assertEquals(3, result.get("total"));
            assertEquals(2L, records.get(0).get("id"));
        }
    }

    @Nested
    @DisplayName("getKnowledgePointLearningStatistics 方法测试")
    class GetStatisticsTests {

        @Test
        @DisplayName("成功获取并计算统计数据")
        void getKnowledgePointLearningStatistics_success() {
            // Arrange
            User student2 = new User();
            student2.setId(2L);
            
            // V-- 修正后的代码
            LearningRecord r1 = new LearningRecord();
            r1.setId(1L);
            r1.setStudent(studentUser);
            r1.setKnowledgePoint(knowledgePoint);
            r1.setActionType(LearningRecord.ActionType.VIEW);
            r1.setDuration(10);
            r1.setLearningTime(LocalDateTime.now());
            r1.setCompleted(false);

            LearningRecord r2 = new LearningRecord();
            r2.setId(2L);
            r2.setStudent(studentUser);
            r2.setKnowledgePoint(knowledgePoint);
            r2.setActionType(LearningRecord.ActionType.PRACTICE);
            r2.setDuration(20);
            r2.setLearningTime(LocalDateTime.now());
            r2.setCompleted(true);
            r2.setScore(80);

            LearningRecord r3 = new LearningRecord();
            r3.setId(3L);
            r3.setStudent(student2);
            r3.setKnowledgePoint(knowledgePoint);
            r3.setActionType(LearningRecord.ActionType.PRACTICE);
            r3.setDuration(30);
            r3.setLearningTime(LocalDateTime.now());
            r3.setCompleted(true);
            r3.setScore(90);
            
            List<LearningRecord> mockRecords = Arrays.asList(r1, r2, r3);

            when(learningRecordRepository.findByKnowledgePointId(knowledgePointId)).thenReturn(mockRecords);

            // Act
            Map<String, Object> result = learningRecordService.getKnowledgePointLearningStatistics(knowledgePointId, currentUser);

            // Assert
            assertTrue((Boolean) result.get("success"));
            Map<String, Object> stats = (Map<String, Object>) result.get("statistics");
            assertEquals(3, stats.get("totalRecords"));
            assertEquals(60, stats.get("totalDuration"));
            assertEquals(2L, stats.get("studentCount"));
            assertEquals(66.67, (Double) stats.get("completionRate"), 0.01);
            assertEquals(85.0, (Double) stats.get("averageScore"), 0.01);
            
            Map<String, Long> actionCounts = (Map<String, Long>) stats.get("actionTypeCounts");
            assertEquals(1L, actionCounts.get("VIEW"));
            assertEquals(2L, actionCounts.get("PRACTICE"));
        }

        @Test
        @DisplayName("无记录时返回空的统计数据")
        void getKnowledgePointLearningStatistics_noRecords_shouldReturnEmptyStats() {
            // Arrange
            when(learningRecordRepository.findByKnowledgePointId(knowledgePointId)).thenReturn(Collections.emptyList());

            // Act
            Map<String, Object> result = learningRecordService.getKnowledgePointLearningStatistics(knowledgePointId, currentUser);

            // Assert
            assertTrue((Boolean) result.get("success"));
            Map<String, Object> stats = (Map<String, Object>) result.get("statistics");
            assertEquals(0, stats.get("totalRecords"));
            assertEquals(0, stats.get("totalDuration"));
            assertEquals(0L, stats.get("studentCount"));
            assertEquals(0.0, stats.get("completionRate"));
            assertNull(stats.get("averageScore"));
        }
    }
}