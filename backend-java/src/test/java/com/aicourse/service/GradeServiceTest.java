package com.aicourse.service;

import com.aicourse.entity.*;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.SubmissionRepository;
import com.aicourse.repository.TaskRepository;
import com.aicourse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradeService Unit Tests")
class GradeServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GradeService gradeService;

    private User admin, teacher, student;
    private Course course;
    private Task task;
    private Submission submission;

    @BeforeEach
    void setUp() {
        admin = createUser(1L, "admin", "Admin User", UserRole.ADMIN);
        teacher = createUser(2L, "teacher", "Teacher User", UserRole.TEACHER);
        student = createUser(3L, "student", "Student User", UserRole.STUDENT);
        student.setStudentId("S001");

        course = new Course();
        course.setId(101L);
        course.setName("Test Course");

        task = new Task();
        task.setId(201L);
        task.setTitle("Test Task");
        task.setCourseId(course.getId());
        task.setTaskType(Task.taskType.ASSIGNMENT);
        task.setMaxScore(100.0);

        submission = new Submission();
        submission.setId(301L);
        submission.setStudentId(student.getId());
        submission.setTaskId(task.getId());
        submission.setScore(85.0);
    }

    private User createUser(Long id, String username, String fullName, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);
        return user;
    }

    // =================== getGrades Tests ===================

    @Test
    @DisplayName("getGrades - As Admin with all filters")
    void getGrades_asAdminWithFilters() {
        Page<Submission> submissionPage = new PageImpl<>(List.of(submission));
        when(submissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(submissionPage);
        
        Page<Submission> result = gradeService.getGrades(Pageable.unpaged(), course.getId(), student.getId(), admin);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(submissionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getGrades - As Student, filters are restricted to self")
    void getGrades_asStudent() {
        Page<Submission> submissionPage = new PageImpl<>(List.of(submission));
        when(submissionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(submissionPage);
        
        // studentId filter will be overridden by the security logic
        Page<Submission> result = gradeService.getGrades(Pageable.unpaged(), null, 99L, student);
        
        assertNotNull(result);
        verify(submissionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    // =================== formatGradeList Tests ===================

    @Test
    @DisplayName("formatGradeList - Success with all related data")
    void formatGradeList_success() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<Map<String, Object>> result = gradeService.formatGradeList(List.of(submission));
        
        assertEquals(1, result.size());
        Map<String, Object> gradeData = result.get(0);
        assertEquals("Student User", gradeData.get("student_name"));
        assertEquals("Test Task", gradeData.get("task_name"));
        assertEquals("Test Course", gradeData.get("course_name"));
    }

    @Test
    @DisplayName("formatGradeList - Handles missing related data gracefully")
    void formatGradeList_handlesMissingData() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        when(taskRepository.findById(any())).thenReturn(Optional.empty());
        
        List<Map<String, Object>> result = gradeService.formatGradeList(List.of(submission));
        
        assertEquals(1, result.size());
        Map<String, Object> gradeData = result.get(0);
        assertEquals("未知学生", gradeData.get("student_name"));
        assertEquals("未知任务", gradeData.get("task_name"));
        assertEquals("未知课程", gradeData.get("course_name"));
    }

    // =================== exportGradesAsCsv Tests ===================

    @Test
    @DisplayName("exportGradesAsCsv - Success by Teacher")
    void exportGradesAsCsv_success() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenReturn(List.of(submission));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        String csv = gradeService.exportGradesAsCsv(course.getId(), teacher);
        
        assertTrue(csv.startsWith("学生姓名,学号,课程名称,任务名称,任务类型,得分,满分,提交时间,批改时间,反馈"));
        assertTrue(csv.contains("Student User,S001,Test Course"));
    }
    
    @Test
    @DisplayName("exportGradesAsCsv - Skips problematic records")
    void exportGradesAsCsv_skipsProblematicRecords() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenReturn(List.of(submission));
        // Simulate an exception by returning empty optional
        when(userRepository.findById(any())).thenThrow(new RuntimeException("DB error"));

        String csv = gradeService.exportGradesAsCsv(course.getId(), teacher);
        
        // Should only contain the header
        assertEquals("学生姓名,学号,课程名称,任务名称,任务类型,得分,满分,提交时间,批改时间,反馈\n", csv);
    }

    @Test
    @DisplayName("exportGradesAsCsv - Fails by Student")
    void exportGradesAsCsv_failsByStudent() {
        assertThrows(BusinessException.class, () -> gradeService.exportGradesAsCsv(course.getId(), student));
    }

    // =================== updateGrade Tests ===================

    @Test
    @DisplayName("updateGrade - Success by Admin")
    void updateGrade_success() {
        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        
        Map<String, Object> data = Map.of("score", 95.0, "feedback", "Excellent!");
        Submission result = gradeService.updateGrade(submission.getId(), data, admin);
        
        assertEquals(95.0, result.getScore());
        assertEquals("Excellent!", result.getFeedback());
        assertNotNull(result.getGradedAt());
    }

    @Test
    @DisplayName("updateGrade - Fails by Student")
    void updateGrade_failsByStudent() {
        assertThrows(BusinessException.class, () -> gradeService.updateGrade(submission.getId(), new HashMap<>(), student));
    }

    @Test
    @DisplayName("updateGrade - Submission not found")
    void updateGrade_notFound() {
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> gradeService.updateGrade(99L, new HashMap<>(), teacher));
    }

    // =================== getGradeStatistics Tests ===================

    @Test
    @DisplayName("getGradeStatistics - For Teacher")
    void getGradeStatistics_forTeacher() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenReturn(List.of(submission));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(taskRepository.countByCourseId(course.getId())).thenReturn(1L);

        Map<String, Object> stats = gradeService.getGradeStatistics(course.getId(), teacher);
        
        assertEquals(85.0, stats.get("average_score"));
        assertEquals(85.0, stats.get("highest_score"));
        assertEquals(100.0, stats.get("pass_rate"));
        assertEquals("Student User", stats.get("top_student"));
    }

    @Test
    @DisplayName("getGradeStatistics - For Student")
    void getGradeStatistics_forStudent() {
        when(submissionRepository.findGradedSubmissionsByStudentAndCourse(student.getId(), course.getId())).thenReturn(List.of(submission));
        
        Map<String, Object> stats = gradeService.getGradeStatistics(course.getId(), student);
        
        assertNotNull(stats);
        verify(submissionRepository).findGradedSubmissionsByStudentAndCourse(student.getId(), course.getId());
    }

    @Test
    @DisplayName("getGradeStatistics - Empty submissions")
    void getGradeStatistics_emptySubmissions() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenReturn(Collections.emptyList());
        Map<String, Object> stats = gradeService.getGradeStatistics(course.getId(), teacher);
        assertEquals(0.0, stats.get("average_score"));
        assertEquals(0, stats.get("submitted_count"));
    }
    
    @Test
    @DisplayName("getGradeStatistics - Exception handling")
    void getGradeStatistics_exceptionHandling() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenThrow(new RuntimeException("DB Error"));
        Map<String, Object> stats = gradeService.getGradeStatistics(course.getId(), teacher);
        assertEquals(0.0, stats.get("average_score"));
    }

    // =================== getAiGradeAnalysis Tests ===================

    @Test
    @DisplayName("getAiGradeAnalysis - Success with courseId")
    void getAiGradeAnalysis_successWithCourseId() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenReturn(List.of(submission));
        Map<String, Object> result = gradeService.getAiGradeAnalysis(course.getId(), null, teacher);
        
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("statistics"));
        assertNotNull(result.get("ai_analysis"));
    }
    
    @Test
    @DisplayName("getAiGradeAnalysis - Success with taskId")
    void getAiGradeAnalysis_successWithTaskId() {
        when(submissionRepository.findGradedSubmissionsByTask(task.getId())).thenReturn(List.of(submission));
        Map<String, Object> result = gradeService.getAiGradeAnalysis(null, task.getId(), teacher);
        
        assertTrue((Boolean) result.get("success"));
    }
    
    @Test
    @DisplayName("getAiGradeAnalysis - Success with no filters")
    void getAiGradeAnalysis_successWithNoFilters() {
        when(submissionRepository.findGradedSubmissions()).thenReturn(List.of(submission));
        Map<String, Object> result = gradeService.getAiGradeAnalysis(null, null, teacher);
        
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("getAiGradeAnalysis - Fails by Student")
    void getAiGradeAnalysis_failsByStudent() {
        assertThrows(BusinessException.class, () -> gradeService.getAiGradeAnalysis(course.getId(), null, student));
    }

    @Test
    @DisplayName("getAiGradeAnalysis - No data found")
    void getAiGradeAnalysis_noData() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenReturn(Collections.emptyList());
        Map<String, Object> result = gradeService.getAiGradeAnalysis(course.getId(), null, teacher);
        
        assertFalse((Boolean) result.get("success"));
        assertEquals("没有找到相关成绩数据", result.get("message"));
    }
    
    @Test
    @DisplayName("getAiGradeAnalysis - Exception handling")
    void getAiGradeAnalysis_exceptionHandling() {
        when(submissionRepository.findGradedSubmissionsByCourse(course.getId())).thenThrow(new RuntimeException("DB Error"));
        Map<String, Object> result = gradeService.getAiGradeAnalysis(course.getId(), null, teacher);
        
        assertFalse((Boolean) result.get("success"));
        assertEquals("DB Error", result.get("error"));
    }
    
    @Test
    @DisplayName("generateBasicAnalysis - All branches")
    void generateBasicAnalysis_allBranches() {
        // Test all branches of the private method generateBasicAnalysis
        gradeService.getAiGradeAnalysis(null, null, teacher); // Default case
        
        Submission s1 = new Submission(); s1.setScore(95.0);
        Submission s2 = new Submission(); s2.setScore(85.0);
        Submission s3 = new Submission(); s3.setScore(75.0);
        Submission s4 = new Submission(); s4.setScore(65.0);
        Submission s5 = new Submission(); s5.setScore(55.0);
        
        when(submissionRepository.findGradedSubmissions()).thenReturn(List.of(s1, s2, s3, s4, s5));
        
        // Test different average scores
        when(submissionRepository.findGradedSubmissions()).thenReturn(List.of(s1));
        gradeService.getAiGradeAnalysis(null, null, teacher);
        
        when(submissionRepository.findGradedSubmissions()).thenReturn(List.of(s2));
        gradeService.getAiGradeAnalysis(null, null, teacher);
        
        when(submissionRepository.findGradedSubmissions()).thenReturn(List.of(s3));
        gradeService.getAiGradeAnalysis(null, null, teacher);

        when(submissionRepository.findGradedSubmissions()).thenReturn(List.of(s5));
        gradeService.getAiGradeAnalysis(null, null, teacher);
    }
}
