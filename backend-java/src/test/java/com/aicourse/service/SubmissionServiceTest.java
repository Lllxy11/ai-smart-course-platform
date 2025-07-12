package com.aicourse.service;

import com.aicourse.entity.Course;
import com.aicourse.entity.Submission;
import com.aicourse.entity.Task;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private User studentUser, anotherStudentUser, teacherUser, adminUser;
    private Task task;
    private Submission submission1, submission2;

    @BeforeEach
    void setUp() {
        // 创建不同角色的用户
        studentUser = new User("student@test.com", "student", "学生小明", "password", UserRole.STUDENT);
        studentUser.setId(1L);

        anotherStudentUser = new User("another@test.com", "another", "学生小红", "password", UserRole.STUDENT);
        anotherStudentUser.setId(2L);

        teacherUser = new User("teacher@test.com", "teacher", "教师张三", "password", UserRole.TEACHER);
        teacherUser.setId(10L);
        
        adminUser = new User("admin@test.com", "admin", "管理员", "password", UserRole.ADMIN);
        adminUser.setId(100L);

        // 创建课程和任务
        Course course = new Course();
        course.setId(1L);
        course.setName("测试课程");

        task = new Task();
        task.setId(1L);
        task.setTitle("测试任务");
        task.setCourse(course);
        task.setCreator(teacherUser); // 任务由教师创建
        task.setMaxScore(100.0);
        
        // 创建提交记录
        submission1 = new Submission();
        submission1.setId(1L);
        submission1.setTask(task);
        submission1.setStudent(studentUser);
        submission1.setSubmittedAt(LocalDateTime.now());
        // 已修改：根据您的实体类，初始状态应为 SUBMITTED
        submission1.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission1.setStudentId(studentUser.getId());
        submission1.setTaskId(task.getId());


        submission2 = new Submission();
        submission2.setId(2L);
        submission2.setTask(task);
        submission2.setStudent(anotherStudentUser);
        submission2.setSubmittedAt(LocalDateTime.now());
        // 已修改：根据您的实体类，初始状态应为 SUBMITTED
        submission2.setStatus(Submission.SubmissionStatus.SUBMITTED);
        submission2.setStudentId(anotherStudentUser.getId());
        submission2.setTaskId(task.getId());
    }

    @Test
    @DisplayName("获取提交列表 - 教师获取所有提交")
    void getSubmissions_AsTeacher_ShouldReturnAllSubmissions() {
        // Arrange
        when(submissionRepository.findAll()).thenReturn(Arrays.asList(submission1, submission2));
        
        // Act
        Map<String, Object> result = submissionService.getSubmissions(task.getId(), null, 0, 10, teacherUser);

        // Assert
        assertEquals(2, result.get("total"));
        List<Map<String, Object>> grades = (List<Map<String, Object>>) result.get("grades");
        assertEquals(2, grades.size());
        verify(submissionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("获取提交列表 - 学生只能获取自己的提交")
    void getSubmissions_AsStudent_ShouldReturnOnlyOwnSubmissions() {
        // Arrange
        when(submissionRepository.findAll()).thenReturn(Arrays.asList(submission1, submission2));
        
        // Act
        Map<String, Object> result = submissionService.getSubmissions(null, null, 0, 10, studentUser);

        // Assert
        assertEquals(1, result.get("total"));
        List<Map<String, Object>> grades = (List<Map<String, Object>>) result.get("grades");
        assertEquals(1, grades.size());
        assertEquals(studentUser.getId(), grades.get(0).get("userId"));
    }

    @Test
    @DisplayName("获取单个提交 - 成功获取")
    void getSubmission_Success() {
        // Arrange
        when(submissionRepository.findById(submission1.getId())).thenReturn(Optional.of(submission1));
        
        // Act
        Map<String, Object> result = submissionService.getSubmission(submission1.getId(), teacherUser);

        // Assert
        assertNotNull(result);
        assertEquals(submission1.getId(), result.get("id"));
        assertEquals(studentUser.getFullName(), result.get("studentName"));
    }

    @Test
    @DisplayName("获取单个提交 - 提交不存在应抛出异常")
    void getSubmission_NotFound_ShouldThrowException() {
        // Arrange
        when(submissionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            submissionService.getSubmission(99L, teacherUser);
        });
    }

    @Test
    @DisplayName("获取单个提交 - 学生无权访问他人提交应抛出异常")
    void getSubmission_StudentAccessDenied_ShouldThrowException() {
        // Arrange
        when(submissionRepository.findById(submission2.getId())).thenReturn(Optional.of(submission2));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            submissionService.getSubmission(submission2.getId(), studentUser);
        });
        assertEquals("无权访问此提交", exception.getMessage());
    }

    @Test
    @DisplayName("批改作业 - 教师成功批改")
    void gradeSubmission_AsTeacher_Success() {
        // Arrange
        when(submissionRepository.findById(submission1.getId())).thenReturn(Optional.of(submission1));
        double score = 95.5;
        String feedback = "完成得很好！";

        // Act
        submissionService.gradeSubmission(submission1.getId(), score, feedback, teacherUser);

        // Assert
        ArgumentCaptor<Submission> submissionCaptor = ArgumentCaptor.forClass(Submission.class);
        verify(submissionRepository, times(1)).save(submissionCaptor.capture());
        
        Submission savedSubmission = submissionCaptor.getValue();
        assertEquals(score, savedSubmission.getScore());
        assertEquals(feedback, savedSubmission.getFeedback());
        assertEquals(Submission.SubmissionStatus.GRADED, savedSubmission.getStatus());
        assertNotNull(savedSubmission.getGradedAt());
    }
    
    @Test
    @DisplayName("批改作业 - 学生无权批改应抛出异常")
    void gradeSubmission_AsStudent_ShouldThrowException() {
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            submissionService.gradeSubmission(submission1.getId(), 90.0, "feedback", studentUser);
        });
        assertEquals("无权批改作业", exception.getMessage());
    }

    @Test
    @DisplayName("删除提交 - 学生删除自己的提交")
    void deleteSubmission_StudentDeletesOwn_Success() {
        // Arrange
        when(submissionRepository.findById(submission1.getId())).thenReturn(Optional.of(submission1));
        
        // Act
        submissionService.deleteSubmission(submission1.getId(), studentUser);

        // Assert
        verify(submissionRepository, times(1)).delete(submission1);
    }
    
    @Test
    @DisplayName("删除提交 - 教师可以删除任何提交")
    void deleteSubmission_TeacherDeletes_Success() {
        // Arrange
        when(submissionRepository.findById(submission1.getId())).thenReturn(Optional.of(submission1));
        
        // Act
        submissionService.deleteSubmission(submission1.getId(), teacherUser);

        // Assert
        verify(submissionRepository, times(1)).delete(submission1);
    }
    
    @Test
    @DisplayName("删除提交 - 学生无权删除他人提交应抛出异常")
    void deleteSubmission_StudentDeletesOther_ShouldThrowException() {
        // Arrange
        when(submissionRepository.findById(submission2.getId())).thenReturn(Optional.of(submission2));
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            submissionService.deleteSubmission(submission2.getId(), studentUser);
        });
        assertEquals("无权删除此提交", exception.getMessage());
    }

    @Test
    @DisplayName("批量评分 - 成功完成")
    void batchGrade_Success() {
        // Arrange
        Map<String, Object> submissionData1 = new HashMap<>();
        submissionData1.put("submissionId", submission1.getId());
        submissionData1.put("score", 88.0);
        submissionData1.put("feedback", "Good");

        Map<String, Object> submissionData2 = new HashMap<>();
        submissionData2.put("submissionId", submission2.getId());
        submissionData2.put("score", 92.0);
        submissionData2.put("feedback", "Excellent");

        Map<String, Object> batchData = new HashMap<>();
        batchData.put("submissions", Arrays.asList(submissionData1, submissionData2));

        when(submissionRepository.findById(submission1.getId())).thenReturn(Optional.of(submission1));
        when(submissionRepository.findById(submission2.getId())).thenReturn(Optional.of(submission2));

        // Act
        Map<String, Object> result = submissionService.batchGrade(batchData, teacherUser);

        // Assert
        assertEquals("批量评分完成", result.get("message"));
        assertEquals(2, result.get("successCount"));
        assertEquals(0, result.get("failureCount"));
        verify(submissionRepository, times(2)).save(any(Submission.class));
    }

    @Test
    @DisplayName("批量评分 - 部分成功部分失败")
    void batchGrade_PartialSuccess() {
        // Arrange
        Map<String, Object> submissionData1 = new HashMap<>();
        submissionData1.put("submissionId", submission1.getId());
        submissionData1.put("score", 88.0);
        submissionData1.put("feedback", "Good");

        Map<String, Object> submissionDataInvalid = new HashMap<>();
        submissionDataInvalid.put("submissionId", 99L); // 不存在的ID
        submissionDataInvalid.put("score", 92.0);
        submissionDataInvalid.put("feedback", "Excellent");

        Map<String, Object> batchData = new HashMap<>();
        batchData.put("submissions", Arrays.asList(submissionData1, submissionDataInvalid));

        when(submissionRepository.findById(submission1.getId())).thenReturn(Optional.of(submission1));
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty()); // 模拟找不到

        // Act
        Map<String, Object> result = submissionService.batchGrade(batchData, teacherUser);
        
        // Assert
        assertEquals(1, result.get("successCount"));
        assertEquals(1, result.get("failureCount"));
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    @DisplayName("获取提交统计 - 教师获取统计数据")
    void getSubmissionStatistics_AsTeacher_Success() {
        // Arrange
        submission1.setScore(90.0); // 批改过的
        submission2.setStatus(Submission.SubmissionStatus.SUBMITTED); // 未批改的
        List<Submission> submissions = Arrays.asList(submission1, submission2);
        
        // 模拟教师是任务创建者
        submission1.getTask().setCreator(teacherUser);
        submission2.getTask().setCreator(teacherUser);

        when(submissionRepository.findAll()).thenReturn(submissions);

        // Act
        Map<String, Object> stats = submissionService.getSubmissionStatistics(task.getId(), null, teacherUser);

        // Assert
        assertEquals(2L, stats.get("totalSubmissions"));
        assertEquals(1L, stats.get("gradedSubmissions"));
        assertEquals(1L, stats.get("pendingSubmissions"));
        assertEquals(90.0, stats.get("averageScore"));
        assertEquals(50.0, stats.get("gradingRate"));
    }

    @Test
    @DisplayName("获取提交统计 - 学生无权访问应抛出异常")
    void getSubmissionStatistics_AsStudent_ShouldThrowException() {
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            submissionService.getSubmissionStatistics(task.getId(), null, studentUser);
        });
        assertEquals("无权访问统计数据", exception.getMessage());
    }

    @Test
    @DisplayName("学生查自己提交 - 成功获取")
    void getSubmissionsByStudent_Success() {
        // Arrange
        when(submissionRepository.findByTaskIdAndStudentId(task.getId(), studentUser.getId())).thenReturn(Collections.singletonList(submission1));

        // Act
        List<Submission> result = submissionService.getSubmissionsByStudent(task.getId(), studentUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(submission1.getId(), result.get(0).getId());
        verify(submissionRepository).findByTaskIdAndStudentId(task.getId(), studentUser.getId());
    }

    @Test
    @DisplayName("教师查所有提交 - 成功获取")
    void getSubmissionsByTask_Success() {
        // Arrange
        when(submissionRepository.findByTaskId(task.getId())).thenReturn(Arrays.asList(submission1, submission2));
        
        // Act
        List<Submission> result = submissionService.getSubmissionsByTask(task.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(submissionRepository).findByTaskId(task.getId());
    }
}