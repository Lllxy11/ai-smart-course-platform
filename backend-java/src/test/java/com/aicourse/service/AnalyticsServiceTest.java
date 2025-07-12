package com.aicourse.service;

import com.aicourse.entity.*;
import com.aicourse.exception.BusinessException;
import com.aicourse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

@Mock
private CourseRepository courseRepository;

@Mock
private UserRepository userRepository;

@Mock
private TaskRepository taskRepository;

@Mock
private SubmissionRepository submissionRepository;

@InjectMocks
private AnalyticsService analyticsService;

private User adminUser;
private User teacherUser;
private User studentUser;
private User anotherStudentUser;

@BeforeEach
void setUp() {
// 初始化测试中使用的通用用户对象
adminUser = createUser(1L, "Admin User", UserRole.ADMIN);
teacherUser = createUser(2L, "Teacher User", UserRole.TEACHER);
studentUser = createUser(3L, "Student User", UserRole.STUDENT);
anotherStudentUser = createUser(4L, "Another Student", UserRole.STUDENT);
}

// =================== getDashboardStats Tests ===================

@Test
@DisplayName("getDashboardStats - Should return comprehensive dashboard statistics")
void getDashboardStats_shouldReturnComprehensiveStats() {
// --- Mocking ---
when(userRepository.count()).thenReturn(150L);
when(courseRepository.count()).thenReturn(20L);
when(taskRepository.count()).thenReturn(100L);
when(submissionRepository.count()).thenReturn(500L);
when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(5L);
when(userRepository.countByRole(UserRole.TEACHER)).thenReturn(25L);
when(userRepository.countByRole(UserRole.STUDENT)).thenReturn(120L);

// Mock a list of users for online user calculation
List<User> users = new ArrayList<>();
users.add(createUserWithLogin(1L, UserRole.STUDENT, LocalDateTime.now().minusHours(1))); // Online
users.add(createUserWithLogin(2L, UserRole.TEACHER, LocalDateTime.now().minusHours(25))); // Offline
when(userRepository.findAll()).thenReturn(users);

// Mock submissions for pending grading and completion rate calculation
List<Submission> submissions = new ArrayList<>();
submissions.add(createSubmission(1L, 101L, 3L, 85.0, LocalDateTime.now())); // Graded
submissions.add(createSubmission(2L, 102L, 3L, null, LocalDateTime.now())); // Pending
when(submissionRepository.findAll()).thenReturn(submissions);

// --- Execution ---
Map<String, Object> stats = analyticsService.getDashboardStats(adminUser);

// --- Assertions ---
assertNotNull(stats);
assertEquals(150L, stats.get("totalUsers"));
assertEquals(20L, stats.get("totalCourses"));
assertEquals(1L, stats.get("onlineUsers"));
assertEquals(5L, stats.get("adminCount"));
assertEquals(25L, stats.get("teacherCount"));
assertEquals(120L, stats.get("studentCount"));
assertEquals("12000", stats.get("revenue"));
assertEquals(1L, stats.get("dailyActive"));
assertEquals(50L, stats.get("courseCompletion")); // 1 graded / 2 total
assertEquals(500L, stats.get("homeworkSubmission")); // 500 total submissions / 100 total tasks

List<Map<String, Object>> pendingTasks = (List<Map<String, Object>>) stats.get("pendingTasks");
assertEquals(1, pendingTasks.size());
assertTrue(((String)pendingTasks.get(0).get("description")).contains("1 份作业等待评分"));
}

// =================== getUserDashboardStats Tests ===================

@Test
@DisplayName("getUserDashboardStats - For Admin Role")
void getUserDashboardStats_forAdmin() {
when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
when(courseRepository.count()).thenReturn(10L);
when(userRepository.countByRole(UserRole.STUDENT)).thenReturn(50L);
when(submissionRepository.findAll()).thenReturn(Collections.singletonList(createSubmission(1L, 101L, 3L, 90.0, LocalDateTime.now())));
when(submissionRepository.findGradedSubmissions()).thenReturn(Collections.singletonList(createSubmission(1L, 101L, 3L, 90.0, LocalDateTime.now())));

Map<String, Object> stats = analyticsService.getUserDashboardStats(adminUser.getId(), adminUser);

assertEquals("admin", stats.get("role"));
assertEquals(10L, stats.get("total_courses"));
assertEquals(50L, stats.get("total_students"));
assertEquals(0L, stats.get("pending_tasks"));
assertEquals(90.0, stats.get("average_score"));
}

@Test
@DisplayName("getUserDashboardStats - For Teacher Role")
void getUserDashboardStats_forTeacher() {
when(userRepository.findById(teacherUser.getId())).thenReturn(Optional.of(teacherUser));
List<Course> courses = Collections.singletonList(createCourse(1L, teacherUser.getId()));
when(courseRepository.findByTeacherId(teacherUser.getId())).thenReturn(courses);
when(courseRepository.countStudentsByCourseId(anyLong())).thenReturn(30L);
when(submissionRepository.findByTaskCreatorId(teacherUser.getId())).thenReturn(
Arrays.asList(
createSubmission(1L, 101L, 3L, 80.0, LocalDateTime.now()),
createSubmission(2L, 102L, 4L, null, LocalDateTime.now())
)
);

Map<String, Object> stats = analyticsService.getUserDashboardStats(teacherUser.getId(), teacherUser);

assertEquals("teacher", stats.get("role"));
assertEquals(1, stats.get("total_courses"));
assertEquals(30L, stats.get("total_students"));
assertEquals(1L, stats.get("pending_tasks"));
assertEquals(80.0, stats.get("average_score"));
}

@Test
@DisplayName("getUserDashboardStats - For Student Role")
void getUserDashboardStats_forStudent() {
when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
when(courseRepository.countStudentCourses(studentUser.getId())).thenReturn(5L);
when(taskRepository.count()).thenReturn(20L);
// Mock unfinished tasks calculation
when(taskRepository.findAll()).thenReturn(createTaskList(20));
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(createSubmissionList(studentUser.getId(), 15));

Map<String, Object> stats = analyticsService.getUserDashboardStats(studentUser.getId(), studentUser);

assertEquals("student", stats.get("role"));
assertEquals(5L, stats.get("enrolled_courses"));
assertEquals(20L, stats.get("total_tasks"));
assertEquals(5L, stats.get("unfinished_tasks")); // 20 total - 15 submitted
assertEquals(15L, stats.get("completed_tasks"));
}

@Test
@DisplayName("getUserDashboardStats - Student Access Denied")
void getUserDashboardStats_studentAccessDenied() {
BusinessException exception = assertThrows(BusinessException.class, () -> {
analyticsService.getUserDashboardStats(anotherStudentUser.getId(), studentUser);
});
assertEquals("只能查看自己的数据", exception.getMessage());
}

@Test
@DisplayName("getUserDashboardStats - User Not Found")
void getUserDashboardStats_userNotFound() {
when(userRepository.findById(999L)).thenReturn(Optional.empty());
assertThrows(BusinessException.class, () -> {
analyticsService.getUserDashboardStats(999L, adminUser);
});
}

// =================== getRecentActivities Tests ===================

@Test
@DisplayName("getRecentActivities - Should return sorted activities")
void getRecentActivities_shouldReturnSortedActivities() {
Submission sub1 = createSubmission(1L, 101L, studentUser.getId(), 90.0, LocalDateTime.now().minusDays(1));
Submission sub2 = createSubmission(2L, 102L, studentUser.getId(), null, LocalDateTime.now());
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(Arrays.asList(sub1, sub2));

Map<String, Object> result = analyticsService.getRecentActivities(studentUser.getId(), 5, studentUser);
List<Map<String, Object>> activities = (List<Map<String, Object>>) result.get("activities");

assertNotNull(activities);
assertEquals(2, activities.size());
// The most recent submission should be first
assertEquals("submission_2", activities.get(0).get("id"));
assertEquals("submission_1", activities.get(1).get("id"));
}

@Test
@DisplayName("getRecentActivities - Student Access Denied")
void getRecentActivities_studentAccessDenied() {
assertThrows(BusinessException.class, () -> {
analyticsService.getRecentActivities(anotherStudentUser.getId(), 5, studentUser);
});
}

// =================== getLearningTrends Tests ===================

@Test
@DisplayName("getLearningTrends - Should calculate trends correctly")
void getLearningTrends_shouldCalculateTrendsCorrectly() {
List<Submission> submissions = Arrays.asList(
createSubmission(1L, 101L, studentUser.getId(), 80.0, LocalDateTime.now()), // Today
createSubmission(2L, 102L, studentUser.getId(), 85.0, LocalDateTime.now()), // Today
createSubmission(3L, 103L, studentUser.getId(), 90.0, LocalDateTime.now().minusDays(2)) // 2 days ago
);
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(submissions);

Map<String, Object> result = analyticsService.getLearningTrends(studentUser.getId(), 7, studentUser);

assertNotNull(result);
assertEquals("7天", result.get("period"));
List<Map<String, Object>> dailyTrends = (List<Map<String, Object>>) result.get("daily_trends");
assertEquals(7, dailyTrends.size());

// Check today's data (last element in the reversed list)
Map<String, Object> todayData = dailyTrends.get(6);
assertEquals(2, todayData.get("tasks_completed"));
assertEquals(120.0, todayData.get("study_time"));

// Check data from 2 days ago
Map<String, Object> twoDaysAgoData = dailyTrends.get(4);
assertEquals(1, twoDaysAgoData.get("tasks_completed"));
assertEquals(60.0, twoDaysAgoData.get("study_time"));

assertEquals(180L, result.get("total_study_time")); // 120 + 60
assertEquals(3, result.get("total_tasks_completed"));
}

// =================== getKnowledgeMastery Tests ===================

@Test
@DisplayName("getKnowledgeMastery - For a specific course")
void getKnowledgeMastery_forSpecificCourse() {
when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
Course course = createCourse(1L, teacherUser.getId());
when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

Task task1 = createTask(101L, 1L, "数据结构-基础", "...");
Task task2 = createTask(102L, 1L, "数据结构-进阶", "...");
when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

Submission sub1 = createSubmission(1L, 101L, studentUser.getId(), 95.0, LocalDateTime.now());
Submission sub2 = createSubmission(2L, 102L, studentUser.getId(), 70.0, LocalDateTime.now());
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(Arrays.asList(sub1, sub2));

Map<String, Object> result = analyticsService.getKnowledgeMastery(studentUser.getId(), 1L, studentUser);
List<Map<String, Object>> masteryList = (List<Map<String, Object>>) result.get("knowledge_mastery");

assertEquals(2, masteryList.size());
Map<String, Object> mastery1 = masteryList.stream().filter(m -> m.get("knowledge_point").equals("数据结构")).findFirst().get();
assertEquals(82.5, mastery1.get("average_score")); // (95+70)/2
assertEquals(2, mastery1.get("mastery_level")); // Familiar
}

@Test
@DisplayName("getKnowledgeMastery - For all courses")
void getKnowledgeMastery_forAllCourses() {
when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
Task task1 = createTask(101L, 1L, "Java编程入门", "...");
Submission sub1 = createSubmission(1L, 101L, studentUser.getId(), 55.0, LocalDateTime.now());
sub1.setTask(task1);
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(Collections.singletonList(sub1));

Map<String, Object> result = analyticsService.getKnowledgeMastery(studentUser.getId(), null, studentUser);
List<Map<String, Object>> masteryList = (List<Map<String, Object>>) result.get("knowledge_mastery");

assertEquals(1, masteryList.size());
assertEquals("Java编程入门", masteryList.get(0).get("knowledge_point"));
assertEquals(55.0, masteryList.get(0).get("average_score"));
assertEquals(0, masteryList.get(0).get("mastery_level")); // Not mastered
assertEquals(55.0, result.get("overall_mastery"));
}

// =================== getWeakPoints Tests ===================

@Test
@DisplayName("getWeakPoints - Should identify and analyze weak points")
void getWeakPoints_shouldIdentifyWeakPoints() {
when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
Task task1 = createTask(101L, 1L, "算法-复杂度分析", "...");
Task task2 = createTask(102L, 1L, "算法-排序", "...");
Submission sub1 = createSubmission(1L, 101L, studentUser.getId(), 50.0, LocalDateTime.now());
sub1.setTask(task1);
Submission sub2 = createSubmission(2L, 102L, studentUser.getId(), 90.0, LocalDateTime.now());
sub2.setTask(task2);
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(Arrays.asList(sub1, sub2));

Map<String, Object> result = analyticsService.getWeakPoints(studentUser.getId(), 5, studentUser);
List<Map<String, Object>> weakPoints = (List<Map<String, Object>>) result.get("weak_points");

assertEquals(1, weakPoints.size());
Map<String, Object> weakPoint = weakPoints.get(0);
assertEquals("算法", weakPoint.get("knowledge_point"));
assertEquals(50.0, weakPoint.get("average_score"));
assertEquals("中等", weakPoint.get("weakness_level"));
assertTrue(((List<String>)weakPoint.get("issues")).contains("理解程度有待提高"));
}

@Test
@DisplayName("getWeakPoints - No weak points found")
void getWeakPoints_noWeakPointsFound() {
when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
Task task1 = createTask(101L, 1L, "Test Task", "...");
Submission sub1 = createSubmission(1L, 101L, studentUser.getId(), 95.0, LocalDateTime.now());
sub1.setTask(task1);
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(Collections.singletonList(sub1));

Map<String, Object> result = analyticsService.getWeakPoints(studentUser.getId(), 5, studentUser);
List<Map<String, Object>> weakPoints = (List<Map<String, Object>>) result.get("weak_points");

assertTrue(weakPoints.isEmpty());
assertEquals("您的学习表现良好，继续保持并适当挑战更高难度的内容！", result.get("overall_suggestion"));
}

// =================== getLearningRecommendations Tests ===================

@Test
@DisplayName("getLearningRecommendations - Recommends review for low scores")
void getLearningRecommendations_recommendsReview() {
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(
Collections.singletonList(createSubmission(1L, 101L, studentUser.getId(), 60.0, LocalDateTime.now()))
);
when(taskRepository.findAll()).thenReturn(new ArrayList<>());

Map<String, Object> result = analyticsService.getLearningRecommendations(studentUser.getId(), studentUser);
List<Map<String, Object>> recs = (List<Map<String, Object>>) result.get("recommendations");

assertEquals(1, recs.size());
assertEquals("rec_low_score", recs.get(0).get("id"));
}

@Test
@DisplayName("getLearningRecommendations - Recommends completing tasks")
void getLearningRecommendations_recommendsCompletingTasks() {
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(new ArrayList<>());
when(taskRepository.findAll()).thenReturn(Collections.singletonList(createTask(101L, 1L, "New Task", "...")));

Map<String, Object> result = analyticsService.getLearningRecommendations(studentUser.getId(), studentUser);
List<Map<String, Object>> recs = (List<Map<String, Object>>) result.get("recommendations");

// It will also generate a general recommendation
assertTrue(recs.stream().anyMatch(r -> r.get("id").equals("rec_incomplete")));
}

@Test
@DisplayName("getLearningRecommendations - Recommends advanced content for high scores")
void getLearningRecommendations_recommendsAdvancedContent() {
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(
Collections.singletonList(createSubmission(1L, 101L, studentUser.getId(), 95.0, LocalDateTime.now()))
);
when(taskRepository.findAll()).thenReturn(Collections.singletonList(createTask(101L, 1L, "Task", "...")));

Map<String, Object> result = analyticsService.getLearningRecommendations(studentUser.getId(), studentUser);
List<Map<String, Object>> recs = (List<Map<String, Object>>) result.get("recommendations");

assertTrue(recs.stream().anyMatch(r -> r.get("id").equals("rec_advanced")));
}

@Test
@DisplayName("getLearningRecommendations - General recommendation when no other conditions met")
void getLearningRecommendations_generalRecommendation() {
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(new ArrayList<>());
when(taskRepository.findAll()).thenReturn(new ArrayList<>());

Map<String, Object> result = analyticsService.getLearningRecommendations(studentUser.getId(), studentUser);
List<Map<String, Object>> recs = (List<Map<String, Object>>) result.get("recommendations");

assertEquals(1, recs.size());
assertEquals("rec_general", recs.get(0).get("id"));
}

// =================== getProgressStats Tests ===================

@Test
@DisplayName("getProgressStats - Should calculate progress correctly")
void getProgressStats_shouldCalculateProgress() {
when(taskRepository.findAll()).thenReturn(createTaskList(10));
List<Submission> submissions = new ArrayList<>();
submissions.add(createSubmissionWithStatus(1L, Submission.SubmissionStatus.SUBMITTED));
submissions.add(createSubmissionWithStatus(2L, Submission.SubmissionStatus.SUBMITTED));
submissions.add(createSubmissionWithStatus(3L, Submission.SubmissionStatus.GRADED)); // Not SUBMITTED status
when(submissionRepository.findByStudentId(studentUser.getId())).thenReturn(submissions);

Map<String, Object> result = analyticsService.getProgressStats(studentUser.getId(), studentUser);

assertEquals(10, result.get("total_tasks"));
assertEquals(2, result.get("completed")); // Only status SUBMITTED counts
assertEquals(8, result.get("in_progress"));
assertEquals(20.0, result.get("completion_rate"));
}

// =================== getTeacherAnalytics Tests ===================

@Test
@DisplayName("getTeacherAnalytics - Should return comprehensive teacher analytics")
void getTeacherAnalytics_shouldReturnAnalytics() {
when(userRepository.findById(teacherUser.getId())).thenReturn(Optional.of(teacherUser));

Course course = createCourse(1L, teacherUser.getId());
CourseEnrollment enrollment = new CourseEnrollment();
enrollment.setStudentId(studentUser.getId());
course.setEnrollments(Collections.singletonList(enrollment));
when(courseRepository.findByTeacherId(teacherUser.getId())).thenReturn(Collections.singletonList(course));

Task task = createTask(101L, 1L, teacherUser.getId(), "Test Task");
when(taskRepository.findByCreatorId(teacherUser.getId())).thenReturn(Collections.singletonList(task));

Submission submission = createSubmission(1L, 101L, studentUser.getId(), 88.0, LocalDateTime.now().minusDays(1));
submission.setTask(task);
when(submissionRepository.findAll()).thenReturn(Collections.singletonList(submission));

Map<String, Object> result = analyticsService.getTeacherAnalytics(teacherUser.getId(), teacherUser);

assertNotNull(result);
Map<String, Object> courseStats = (Map<String, Object>) result.get("course_stats");
assertEquals(1, courseStats.get("total_courses"));
assertEquals(1, courseStats.get("total_students"));

Map<String, Object> taskStats = (Map<String, Object>) result.get("task_stats");
assertEquals(1, taskStats.get("total_tasks"));
assertEquals(0L, taskStats.get("pending_grading"));

Map<String, Object> gradeStats = (Map<String, Object>) result.get("grade_stats");
assertEquals(88.0, gradeStats.get("average_score"));
assertEquals(100.0, gradeStats.get("pass_rate"));

List<Map<String, Object>> weeklyStats = (List<Map<String, Object>>) result.get("weekly_stats");
assertEquals(7, weeklyStats.size());
}

@Test
@DisplayName("getTeacherAnalytics - Access Denied for Student")
void getTeacherAnalytics_accessDeniedForStudent() {
assertThrows(BusinessException.class, () -> {
analyticsService.getTeacherAnalytics(teacherUser.getId(), studentUser);
});
}

@Test
@DisplayName("getTeacherAnalytics - Access Denied for another Teacher")
void getTeacherAnalytics_accessDeniedForAnotherTeacher() {
User anotherTeacher = createUser(99L, "Another Teacher", UserRole.TEACHER);
assertThrows(BusinessException.class, () -> {
analyticsService.getTeacherAnalytics(teacherUser.getId(), anotherTeacher);
});
}

@Test
@DisplayName("getTeacherAnalytics - User is not a Teacher")
void getTeacherAnalytics_userIsNotTeacher() {
when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
assertThrows(BusinessException.class, () -> {
analyticsService.getTeacherAnalytics(studentUser.getId(), adminUser);
});
}

// =================== Test for placeholder methods ===================

@Test
@DisplayName("Test placeholder methods to ensure they run without error")
void testPlaceholderMethods() {
assertNotNull(analyticsService.getBehaviorStats(1L, 7, studentUser));
assertNotNull(analyticsService.getLearningEfficiency(1L, 7, studentUser));
assertNotNull(analyticsService.getGradeDistribution(1L, 1L, studentUser));
assertNotNull(analyticsService.getSubjectPerformance(1L, studentUser));
assertNotNull(analyticsService.getGradeTrend(1L, 3, studentUser));
assertNotNull(analyticsService.getTimeDistribution(1L, 7, studentUser));
assertNotNull(analyticsService.getActivityHeatmap(1L, 30, studentUser));
assertNotNull(analyticsService.getDeviceUsage(1L, 30, studentUser));
assertNotNull(analyticsService.getPredictionData(1L, studentUser));
assertNotNull(analyticsService.getCourseAnalytics(1L, adminUser));
assertNotNull(analyticsService.getClassPerformance(1L, adminUser));
assertNotNull(analyticsService.getPlatformUsage(adminUser));
assertNotNull(analyticsService.getSystemMetrics(adminUser));
assertNotNull(analyticsService.getUserStatistics(adminUser));
assertNotNull(analyticsService.getCourseStatistics(adminUser));
assertNotNull(analyticsService.getLearningProgress(adminUser));
assertNotNull(analyticsService.getTaskCompletion(adminUser));
assertNotNull(analyticsService.getExamScores(adminUser));
assertNotNull(analyticsService.getKnowledgePointMastery(adminUser));
assertNotNull(analyticsService.getLearningPathAnalysis(adminUser));
assertNotNull(analyticsService.getCoursePopularity(adminUser));
}

// =================== Helper Methods for creating test data ===================

private User createUser(Long id, String fullName, UserRole role) {
User user = new User();
user.setId(id);
user.setFullName(fullName);
user.setRole(role);
user.setCreatedAt(LocalDateTime.now().minusMonths(1));
return user;
}

private User createUserWithLogin(Long id, UserRole role, LocalDateTime lastLogin) {
User user = createUser(id, "User " + id, role);
user.setLastLogin(lastLogin);
return user;
}

private Course createCourse(Long id, Long teacherId) {
Course course = new Course();
course.setId(id);
course.setName("Test Course " + id);
course.setTeacherId(teacherId);
course.setCreatedAt(LocalDateTime.now());
return course;
}

private Task createTask(Long id, Long courseId, String title, String description) {
Task task = new Task();
task.setId(id);
task.setCourseId(courseId);
task.setTitle(title);
task.setDescription(description);
return task;
}

private Task createTask(Long id, Long courseId, Long creatorId, String title) {
Task task = createTask(id, courseId, title, "Description for " + title);
task.setCreatorId(creatorId);
return task;
}

private Submission createSubmission(Long id, Long taskId, Long studentId, Double score, LocalDateTime submittedAt) {
Submission submission = new Submission();
submission.setId(id);
submission.setTaskId(taskId);
submission.setStudentId(studentId);
submission.setScore(score);
submission.setSubmittedAt(submittedAt);
submission.setTask(createTask(taskId, 1L, "Task " + taskId, "..."));
return submission;
}

private Submission createSubmissionWithStatus(Long id, Submission.SubmissionStatus status) {
Submission submission = new Submission();
submission.setId(id);
submission.setStatus(status);
return submission;
}

private List<Task> createTaskList(int count) {
return LongStream.range(1, count + 1)
.mapToObj(i -> createTask(i, 1L, "Task " + i, "..."))
.collect(Collectors.toList());
}

private List<Submission> createSubmissionList(Long studentId, int count) {
return LongStream.range(1, count + 1)
.mapToObj(i -> createSubmission(i, i, studentId, 80.0, LocalDateTime.now()))
.collect(Collectors.toList());
}
}


