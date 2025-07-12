package com.aicourse.service;

import com.aicourse.entity.ExamPaper;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.ExamPaperRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamService Unit Tests")
class ExamServiceTest {

@Mock
private ExamPaperRepository examPaperRepository;

@InjectMocks
private ExamService examService;

private User admin, teacher, student, otherTeacher;
private ExamPaper publishedExam, draftExam;

@BeforeEach
void setUp() {
admin = createUser(1L, "admin", UserRole.ADMIN);
teacher = createUser(2L, "teacher", UserRole.TEACHER);
student = createUser(3L, "student", UserRole.STUDENT);
otherTeacher = createUser(4L, "other_teacher", UserRole.TEACHER);

publishedExam = new ExamPaper();
publishedExam.setId(1L);
publishedExam.setTitle("Published Exam");
publishedExam.setStatus("published");
publishedExam.setCreatedBy(teacher.getId());
publishedExam.setCourseId(101L);
publishedExam.setStartTime(LocalDateTime.now().minusHours(1));
publishedExam.setEndTime(LocalDateTime.now().plusHours(1));

draftExam = new ExamPaper();
draftExam.setId(2L);
draftExam.setTitle("Draft Exam");
draftExam.setStatus("draft");
draftExam.setCreatedBy(teacher.getId());
draftExam.setCourseId(102L);
}

private User createUser(Long id, String username, UserRole role) {
User user = new User();
user.setId(id);
user.setUsername(username);
user.setRole(role);
return user;
}

// =================== getExams Tests ===================

@Test
@DisplayName("getExams - As Admin gets all exams")
void getExams_asAdmin_shouldGetAll() {
when(examPaperRepository.findAll()).thenReturn(List.of(publishedExam, draftExam));
Map<String, Object> result = examService.getExams(0, 10, null, null, admin);
assertEquals(2, result.get("total"));
}

@Test
@DisplayName("getExams - As Teacher gets own exams")
void getExams_asTeacher_shouldGetOwn() {
draftExam.setCreatedBy(otherTeacher.getId());
when(examPaperRepository.findAll()).thenReturn(List.of(publishedExam, draftExam));
Map<String, Object> result = examService.getExams(0, 10, null, null, teacher);
assertEquals(1, result.get("total"));
assertEquals(publishedExam.getId(), ((List<Map<String, Object>>) result.get("exams")).get(0).get("id"));
}

@Test
@DisplayName("getExams - As Student gets only published exams")
void getExams_asStudent_shouldGetPublished() {
when(examPaperRepository.findAll()).thenReturn(List.of(publishedExam, draftExam));
Map<String, Object> result = examService.getExams(0, 10, null, null, student);
assertEquals(1, result.get("total"));
assertEquals("published", ((List<Map<String, Object>>) result.get("exams")).get(0).get("status"));
}

@Test
@DisplayName("getExams - With filters and pagination")
void getExams_withFiltersAndPagination() {
when(examPaperRepository.findAll()).thenReturn(List.of(publishedExam, draftExam));
Map<String, Object> result = examService.getExams(0, 10, 101L, "published", admin);
assertEquals(1, result.get("total"));
}

// =================== createExam Tests ===================

@Test
@DisplayName("createExam - Success by Teacher")
void createExam_successByTeacher() {
Map<String, Object> data = createExamDataMap();
when(examPaperRepository.save(any(ExamPaper.class))).thenAnswer(i -> i.getArgument(0));
Map<String, Object> result = examService.createExam(data, teacher);
assertEquals("考试创建成功", result.get("message"));
}
@Test
@DisplayName("createExam - With default values")
void createExam_withDefaultValues() {
Map<String, Object> data = new HashMap<>();
data.put("title", "Exam with defaults");
when(examPaperRepository.save(any(ExamPaper.class))).thenAnswer(i -> {
ExamPaper saved = i.getArgument(0);
assertEquals(60, saved.getDuration());
assertEquals(100.0, saved.getTotalScore());
return saved;
});
examService.createExam(data, teacher);
verify(examPaperRepository).save(any(ExamPaper.class));
}

@Test
@DisplayName("createExam - Fails by Student")
void createExam_failsByStudent() {
assertThrows(BusinessException.class, () -> examService.createExam(new HashMap<>(), student));
}

// =================== getExam Tests ===================

@Test
@DisplayName("getExam - Success by creator Teacher")
void getExam_successByCreator() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertNotNull(examService.getExam(1L, teacher));
}

@Test
@DisplayName("getExam - Student access denied for draft exam")
void getExam_studentDeniedForDraft() {
when(examPaperRepository.findById(2L)).thenReturn(Optional.of(draftExam));
assertThrows(BusinessException.class, () -> examService.getExam(2L, student));
}

@Test
@DisplayName("getExam - Teacher access denied for other's exam")
void getExam_teacherDeniedForOthers() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertThrows(BusinessException.class, () -> examService.getExam(1L, otherTeacher));
}

@Test
@DisplayName("getExam - Not Found")
void getExam_notFound() {
when(examPaperRepository.findById(99L)).thenReturn(Optional.empty());
assertThrows(ResourceNotFoundException.class, () -> examService.getExam(99L, admin));
}

// =================== updateExam Tests ===================

@Test
@DisplayName("updateExam - Success by Admin")
void updateExam_successByAdmin() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
Map<String, Object> data = Map.of("title", "Updated Title");
examService.updateExam(1L, data, admin);
verify(examPaperRepository).save(any(ExamPaper.class));
assertEquals("Updated Title", publishedExam.getTitle());
}

@Test
@DisplayName("updateExam - Fails by unauthorized Teacher")
void updateExam_failsByUnauthorizedTeacher() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertThrows(BusinessException.class, () -> examService.updateExam(1L, new HashMap<>(), otherTeacher));
}

@Test
@DisplayName("updateExam - Fails by Student")
void updateExam_failsByStudent() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertThrows(BusinessException.class, () -> examService.updateExam(1L, new HashMap<>(), student));
}

// =================== deleteExam Tests ===================

@Test
@DisplayName("deleteExam - Success by creator Teacher")
void deleteExam_successByCreator() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
examService.deleteExam(1L, teacher);
verify(examPaperRepository).delete(publishedExam);
}

@Test
@DisplayName("deleteExam - Fails by unauthorized user")
void deleteExam_failsByUnauthorized() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertThrows(BusinessException.class, () -> examService.deleteExam(1L, student));
}

// =================== getExamQuestions Tests ===================

@Test
@DisplayName("getExamQuestions - Success")
void getExamQuestions_success() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
Map<String, Object> result = examService.getExamQuestions(1L, student);
assertNotNull(result);
assertEquals(1L, result.get("examId"));
}

// =================== publishExam Tests ===================

@Test
@DisplayName("publishExam - Success by Admin")
void publishExam_successByAdmin() {
when(examPaperRepository.findById(2L)).thenReturn(Optional.of(draftExam));
examService.publishExam(2L, admin);
assertEquals("published", draftExam.getStatus());
verify(examPaperRepository).save(draftExam);
}

@Test
@DisplayName("publishExam - Fails by unauthorized user")
void publishExam_failsByUnauthorized() {
when(examPaperRepository.findById(2L)).thenReturn(Optional.of(draftExam));
assertThrows(BusinessException.class, () -> examService.publishExam(2L, student));
}

// =================== getExamStatistics Tests ===================

@Test
@DisplayName("getExamStatistics - As Admin")
void getExamStatistics_asAdmin() {
when(examPaperRepository.findAll()).thenReturn(List.of(publishedExam, draftExam));
Map<String, Object> stats = examService.getExamStatistics(admin);
assertEquals(2L, stats.get("totalExams"));
assertEquals(1L, stats.get("publishedExams"));
}
@Test
@DisplayName("getExamStatistics - As Teacher")
void getExamStatistics_asTeacher() {
draftExam.setCreatedBy(otherTeacher.getId());
when(examPaperRepository.findAll()).thenReturn(List.of(publishedExam, draftExam));
Map<String, Object> stats = examService.getExamStatistics(teacher);
assertEquals(1L, stats.get("totalExams"));
}

@Test
@DisplayName("getExamStatistics - Fails by Student")
void getExamStatistics_failsByStudent() {
assertThrows(BusinessException.class, () -> examService.getExamStatistics(student));
}

// =================== startExam Tests ===================

@Test
@DisplayName("startExam - Success")
void startExam_success() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
Map<String, Object> result = examService.startExam(1L, student);
assertEquals("考试开始", result.get("message"));
}

@Test
@DisplayName("startExam - Fails if not published")
void startExam_failsIfNotPublished() {
when(examPaperRepository.findById(2L)).thenReturn(Optional.of(draftExam));
assertThrows(BusinessException.class, () -> examService.startExam(2L, student));
}

@Test
@DisplayName("startExam - Fails if not started yet")
void startExam_failsIfNotStartedYet() {
publishedExam.setStartTime(LocalDateTime.now().plusHours(1));
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertThrows(BusinessException.class, () -> examService.startExam(1L, student));
}

@Test
@DisplayName("startExam - Fails if already ended")
void startExam_failsIfEnded() {
publishedExam.setEndTime(LocalDateTime.now().minusHours(1));
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertThrows(BusinessException.class, () -> examService.startExam(1L, student));
}

// =================== submitExam Tests ===================

@Test
@DisplayName("submitExam - Success")
void submitExam_success() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
Map<String, Object> result = examService.submitExam(1L, new HashMap<>(), student);
assertEquals("考试提交成功", result.get("message"));
}

// =================== getExamResults Tests ===================

@Test
@DisplayName("getExamResults - Success by Admin")
void getExamResults_successByAdmin() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
Map<String, Object> result = examService.getExamResults(1L, admin);
assertEquals("Published Exam", result.get("examTitle"));
}
@Test
@DisplayName("getExamResults - Fails by Student")
void getExamResults_failsByStudent() {
when(examPaperRepository.findById(1L)).thenReturn(Optional.of(publishedExam));
assertThrows(BusinessException.class, () -> examService.getExamResults(1L, student));
}

// Helper method to create a valid map for exam creation
private Map<String, Object> createExamDataMap() {
Map<String, Object> data = new HashMap<>();
data.put("title", "New Exam");
data.put("description", "A new exam description.");
data.put("courseId", 101L);
data.put("duration", 90);
data.put("totalScore", 150.0);
data.put("questionCount", 50);
data.put("instructions", "Read carefully.");
data.put("startTime", LocalDateTime.now().plusDays(1).toString());
data.put("endTime", LocalDateTime.now().plusDays(1).plusHours(2).toString());
return data;
}
}


