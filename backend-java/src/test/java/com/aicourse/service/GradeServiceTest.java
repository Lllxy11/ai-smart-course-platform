package com.aicourse.service;
import com.aicourse.entity.*;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.SubmissionRepository;
import com.aicourse.repository.TaskRepository;
import com.aicourse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 测试 getGrades 方法 - 教师角色
    @Test
    void getGrades_teacher() {
        Pageable pageable = mock(Pageable.class);
        Long courseId = 1L;
        Long studentId = 2L;
        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        List<Submission> submissions = new ArrayList<>();
        Submission submission = new Submission();
        submission.setScore(80.0);
        submission.setStudentId(2L);
        Task task = new Task();
        task.setCourseId(1L);
        submission.setTask(task);
        submissions.add(submission);

        Page<Submission> page = new PageImpl<>(submissions);

        when(submissionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Submission> result = gradeService.getGrades(pageable, courseId, studentId, currentUser);

        assertEquals(page, result);
        verify(submissionRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // 测试 getGrades 方法 - 学生角色查看自己的成绩
    @Test
    void getGrades_student_self() {
        Pageable pageable = mock(Pageable.class);
        Long courseId = 1L;
        Long studentId = 2L;
        User currentUser = new User();
        currentUser.setRole(UserRole.STUDENT);
        currentUser.setId(2L);

        List<Submission> submissions = new ArrayList<>();
        Submission submission = new Submission();
        submission.setScore(80.0);
        submission.setStudentId(2L);
        Task task = new Task();
        task.setCourseId(1L);
        submission.setTask(task);
        submissions.add(submission);

        Page<Submission> page = new PageImpl<>(submissions);

        when(submissionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Submission> result = gradeService.getGrades(pageable, courseId, studentId, currentUser);

        assertEquals(page, result);
        verify(submissionRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // 测试 getGrades 方法 - 学生角色尝试查看他人成绩，应抛出异常
    @Test
    void getGrades_student_other() {
        Pageable pageable = mock(Pageable.class);
        Long courseId = 1L;
        Long studentId = 2L;
        User currentUser = new User();
        currentUser.setRole(UserRole.STUDENT);
        currentUser.setId(3L);

        assertThrows(BusinessException.class, () -> gradeService.getGrades(pageable, courseId, studentId, currentUser));
    }

    // 测试 getGrades 方法 - 无查询结果
    @Test
    void getGrades_noResult() {
        Pageable pageable = mock(Pageable.class);
        Long courseId = 1L;
        Long studentId = 2L;
        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        Page<Submission> page = new PageImpl<>(new ArrayList<>());

        when(submissionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Submission> result = gradeService.getGrades(pageable, courseId, studentId, currentUser);

        assertTrue(result.getContent().isEmpty());
        verify(submissionRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // 测试 formatGradeList 方法 - 正常情况
    @Test
    void formatGradeList_normal() {
        List<Submission> submissions = new ArrayList<>();
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setStudentId(2L);
        submission.setTaskId(3L);
        submission.setScore(80.0);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setGradedAt(LocalDateTime.now());
        submission.setFeedback("Good");
        submissions.add(submission);

        User student = new User();
        student.setFullName("John Doe");
        student.setStudentId("12345");

        Task task = new Task();
        task.setTitle("Test Task");
        task.setMaxScore(100.0);
        task.setCourseId(4L);

        Course course = new Course();
        course.setName("Test Course");

        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(courseRepository.findById(4L)).thenReturn(Optional.of(course));

        List<Map<String, Object>> result = gradeService.formatGradeList(submissions);

        assertEquals(1, result.size());
        Map<String, Object> gradeData = result.get(0);
        assertEquals("John Doe", gradeData.get("student_name"));
        assertEquals("12345", gradeData.get("student_id"));
        assertEquals("Test Course", gradeData.get("course_name"));
        assertEquals("Test Task", gradeData.get("task_name"));
        assertEquals("ASSIGNMENT", gradeData.get("task_type"));
        assertEquals(80.0, gradeData.get("score"));
        assertEquals(100.0, gradeData.get("max_score"));
        verify(userRepository, times(1)).findById(2L);
        verify(taskRepository, times(1)).findById(3L);
        verify(courseRepository, times(1)).findById(4L);
    }

    // 测试 formatGradeList 方法 - 学生信息不存在
    @Test
    void formatGradeList_noStudent() {
        List<Submission> submissions = new ArrayList<>();
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setStudentId(2L);
        submission.setTaskId(3L);
        submission.setScore(80.0);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setGradedAt(LocalDateTime.now());
        submission.setFeedback("Good");
        submissions.add(submission);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setMaxScore(100.0);
        task.setCourseId(4L);

        Course course = new Course();
        course.setName("Test Course");

        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(courseRepository.findById(4L)).thenReturn(Optional.of(course));

        List<Map<String, Object>> result = gradeService.formatGradeList(submissions);

        assertEquals(1, result.size());
        Map<String, Object> gradeData = result.get(0);
        assertEquals("未知学生", gradeData.get("student_name"));
        assertEquals("", gradeData.get("student_id"));
        verify(userRepository, times(1)).findById(2L);
        verify(taskRepository, times(1)).findById(3L);
        verify(courseRepository, times(1)).findById(4L);
    }

    // 测试 formatGradeList 方法 - 任务信息不存在
    @Test
    void formatGradeList_noTask() {
        List<Submission> submissions = new ArrayList<>();
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setStudentId(2L);
        submission.setTaskId(3L);
        submission.setScore(80.0);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setGradedAt(LocalDateTime.now());
        submission.setFeedback("Good");
        submissions.add(submission);

        User student = new User();
        student.setFullName("John Doe");
        student.setStudentId("12345");

        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(taskRepository.findById(3L)).thenReturn(Optional.empty());

        List<Map<String, Object>> result = gradeService.formatGradeList(submissions);

        assertEquals(1, result.size());
        Map<String, Object> gradeData = result.get(0);
        assertEquals("John Doe", gradeData.get("student_name"));
        assertEquals("12345", gradeData.get("student_id"));
        assertEquals("未知任务", gradeData.get("task_name"));
        assertEquals("assignment", gradeData.get("task_type"));
        assertEquals(100.0, gradeData.get("max_score"));
        assertEquals("未知课程", gradeData.get("course_name"));
        verify(userRepository, times(1)).findById(2L);
        verify(taskRepository, times(1)).findById(3L);
        verify(courseRepository, never()).findById(anyLong());
    }

    // 测试 exportGradesAsCsv 方法 - 教师角色正常导出
    @Test
    void exportGradesAsCsv_teacher() {
        Long courseId = 1L;
        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        List<Submission> submissions = new ArrayList<>();
        Submission submission = new Submission();
        submission.setStudentId(2L);
        submission.setTaskId(3L);
        submission.setScore(80.0);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setGradedAt(LocalDateTime.now());
        submission.setFeedback("Good");
        submissions.add(submission);

        User student = new User();
        student.setFullName("John Doe");
        student.setStudentId("12345");

        Task task = new Task();
        task.setTitle("Test Task");
        task.setMaxScore(100.0);
        task.setCourseId(1L);

        Course course = new Course();
        course.setName("Test Course");

        when(submissionRepository.findGradedSubmissionsByCourse(courseId)).thenReturn(submissions);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        String result = gradeService.exportGradesAsCsv(courseId, currentUser);

        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("12345"));
        assertTrue(result.contains("Test Course"));
        assertTrue(result.contains("Test Task"));
        assertTrue(result.contains("ASSIGNMENT"));
        assertTrue(result.contains("80.0"));
        assertTrue(result.contains("100.0"));
        verify(submissionRepository, times(1)).findGradedSubmissionsByCourse(courseId);
        verify(userRepository, times(1)).findById(2L);
        verify(taskRepository, times(1)).findById(3L);
        verify(courseRepository, times(1)).findById(1L);
    }

    // 测试 exportGradesAsCsv 方法 - 学生角色尝试导出，应抛出异常
    @Test
    void exportGradesAsCsv_student() {
        Long courseId = 1L;
        User currentUser = new User();
        currentUser.setRole(UserRole.STUDENT);

        assertThrows(BusinessException.class, () -> gradeService.exportGradesAsCsv(courseId, currentUser));
    }

    // 测试 exportGradesAsCsv 方法 - 无提交记录
    @Test
    void exportGradesAsCsv_noSubmissions() {
        Long courseId = 1L;
        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        when(submissionRepository.findGradedSubmissionsByCourse(courseId)).thenReturn(new ArrayList<>());

        String result = gradeService.exportGradesAsCsv(courseId, currentUser);

        assertTrue(result.startsWith("学生姓名,学号,课程名称,任务名称,任务类型,得分,满分,提交时间,批改时间,反馈\n"));
        verify(submissionRepository, times(1)).findGradedSubmissionsByCourse(courseId);
        verify(userRepository, never()).findById(anyLong());
        verify(taskRepository, never()).findById(anyLong());
        verify(courseRepository, never()).findById(anyLong());
    }

    // 测试 updateGrade 方法 - 教师角色正常更新成绩
    @Test
    void updateGrade_teacher() {
        Long submissionId = 1L;
        Map<String, Object> gradeData = new HashMap<>();
        gradeData.put("score", 85.0);
        gradeData.put("feedback", "Great");

        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        Submission submission = new Submission();
        submission.setId(submissionId);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(submission)).thenReturn(submission);

        Submission result = gradeService.updateGrade(submissionId, gradeData, currentUser);

        assertEquals(85.0, result.getScore());
        assertEquals("Great", result.getFeedback());
        assertNotNull(result.getGradedAt());
        verify(submissionRepository, times(1)).findById(submissionId);
        verify(submissionRepository, times(1)).save(submission);
    }

    // 测试 updateGrade 方法 - 学生角色尝试更新成绩，应抛出异常
    @Test
    void updateGrade_student() {
        Long submissionId = 1L;
        Map<String, Object> gradeData = new HashMap<>();
        gradeData.put("score", 85.0);
        gradeData.put("feedback", "Great");

        User currentUser = new User();
        currentUser.setRole(UserRole.STUDENT);

        assertThrows(BusinessException.class, () -> gradeService.updateGrade(submissionId, gradeData, currentUser));
    }

    // 测试 updateGrade 方法 - 提交记录不存在，应抛出异常
    @Test
    void updateGrade_submissionNotFound() {
        Long submissionId = 1L;
        Map<String, Object> gradeData = new HashMap<>();
        gradeData.put("score", 85.0);
        gradeData.put("feedback", "Great");

        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.updateGrade(submissionId, gradeData, currentUser));
        verify(submissionRepository, times(1)).findById(submissionId);
        verify(submissionRepository, never()).save(any());
    }

    // 测试 getGradeStatistics 方法 - 教师角色查看课程成绩统计
    @Test
    void getGradeStatistics_teacher() {
        Long courseId = 1L;
        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        List<Submission> submissions = new ArrayList<>();
        Submission submission1 = new Submission();
        submission1.setScore(80.0);
        submission1.setTaskId(1L);
        submissions.add(submission1);
        Submission submission2 = new Submission();
        submission2.setScore(90.0);
        submission2.setTaskId(2L);
        submissions.add(submission2);

        Task task1 = new Task();
        task1.setMaxScore(100.0);
        Task task2 = new Task();
        task2.setMaxScore(100.0);

        when(submissionRepository.findGradedSubmissionsByCourse(courseId)).thenReturn(submissions);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
        when(taskRepository.countByCourseId(courseId)).thenReturn(2L);

        Map<String, Object> result = gradeService.getGradeStatistics(courseId, currentUser);

        assertEquals(85.0, result.get("average_score"));
        assertEquals(90.0, result.get("highest_score"));
        assertEquals(100.0, result.get("pass_rate"));
        verify(submissionRepository, times(1)).findGradedSubmissionsByCourse(courseId);
        verify(taskRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(1)).countByCourseId(courseId);
    }

    // 测试 getGradeStatistics 方法 - 学生角色查看自己的课程成绩统计
    @Test
    void getGradeStatistics_student() {
        Long courseId = 1L;
        User currentUser = new User();
        currentUser.setRole(UserRole.STUDENT);
        currentUser.setId(1L);

        List<Submission> submissions = new ArrayList<>();
        Submission submission = new Submission();
        submission.setScore(80.0);
        submission.setTaskId(1L);
        submission.setStudentId(1L);
        submissions.add(submission);

        Task task = new Task();
        task.setMaxScore(100.0);

        when(submissionRepository.findGradedSubmissionsByStudentAndCourse(1L, courseId)).thenReturn(submissions);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.countByCourseId(courseId)).thenReturn(1L);

        Map<String, Object> result = gradeService.getGradeStatistics(courseId, currentUser);

        assertEquals(80.0, result.get("average_score"));
        assertEquals(80.0, result.get("highest_score"));
        assertEquals(100.0, result.get("pass_rate"));
        verify(submissionRepository, times(1)).findGradedSubmissionsByStudentAndCourse(1L, courseId);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).countByCourseId(courseId);
    }

    // 测试 getGradeStatistics 方法 - 无提交记录
    @Test
    void getGradeStatistics_noSubmissions() {
        Long courseId = 1L;
        User currentUser = new User();
        currentUser.setRole(UserRole.TEACHER);

        when(submissionRepository.findGradedSubmissionsByCourse(courseId)).thenReturn(new ArrayList<>());

        Map<String, Object> result = gradeService.getGradeStatistics(courseId, currentUser);

        assertEquals(0.0, result.get("average_score"));
        assertEquals(0.0, result.get("highest_score"));
        assertEquals(0.0, result.get("pass_rate"));
        verify(submissionRepository, times(1)).findGradedSubmissionsByCourse(courseId);
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).countByCourseId(anyLong());
    }}

