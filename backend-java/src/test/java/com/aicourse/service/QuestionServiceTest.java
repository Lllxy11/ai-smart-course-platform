package com.aicourse.service;

import com.aicourse.entity.Course;
import com.aicourse.entity.Question;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole; // Assuming you have a UserRole enum
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.QuestionRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService 单元测试")
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private CourseRepository courseRepository;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private QuestionService questionService;

    private User studentUser, teacherUser, adminUser, otherTeacherUser;
    private Question question;
    private Long questionId = 1L;
    private Long courseId = 101L;

    @BeforeEach
    void setUp() {
        teacherUser = new User();
        teacherUser.setId(1L);
        teacherUser.setRole(UserRole.TEACHER);

        studentUser = new User();
        studentUser.setId(2L);
        studentUser.setRole(UserRole.STUDENT);

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setRole(UserRole.ADMIN);

        otherTeacherUser = new User();
        otherTeacherUser.setId(4L);
        otherTeacherUser.setRole(UserRole.TEACHER);

        question = new Question();
        question.setId(questionId);
        question.setCreatedBy(teacherUser.getId());
        question.setCourseId(courseId);
        question.setUsageCount(0);
        question.setOptions("[\"Option A\", \"Option B\"]");
        question.setCorrectAnswers("[\"Option A\"]");
        question.setKnowledgePoints("[\"Java Basics\"]");
        question.setExplanation("This is the explanation.");
    }

    @Nested
    @DisplayName("getQuestions 方法测试")
    class GetQuestionsTests {
        @Test
        @DisplayName("应为不同角色和过滤器调用正确的查询")
        void getQuestions_shouldCallFindAllWithCorrectSpec() {
            Page<Question> emptyPage = new PageImpl<>(Collections.emptyList());
            when(questionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

            questionService.getQuestions(Pageable.unpaged(), "single", "easy", courseId, teacherUser);
            questionService.getQuestions(Pageable.unpaged(), null, null, null, adminUser);
            
            verify(questionRepository, times(2)).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("createQuestion 方法测试")
    class CreateQuestionTests {
        @Test
        @DisplayName("成功创建题目")
        void createQuestion_success() {
            Map<String, Object> data = Map.of(
                "type", "single",
                "content", "What is Java?",
                "options", List.of("A", "B"),
                "correct_answers", List.of("A"),
                "course_id", courseId
            );
            when(questionRepository.save(any(Question.class))).thenReturn(question);

            Question created = questionService.createQuestion(data, teacherUser);

            assertNotNull(created);
            ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
            verify(questionRepository).save(captor.capture());
            assertEquals("single", captor.getValue().getType());
            assertEquals(teacherUser.getId(), captor.getValue().getCreatedBy());
        }

        @Test
        @DisplayName("JSON 处理失败时应抛出 BusinessException")
        void createQuestion_jsonProcessingError_shouldThrowException() throws Exception {
            Map<String, Object> data = Map.of("options", List.of("A", "B"));
            doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(any());
            
            assertThrows(BusinessException.class, () -> questionService.createQuestion(data, teacherUser));
        }
    }

    @Nested
    @DisplayName("getQuestionDetail 方法测试")
    class GetQuestionDetailTests {
        @Test
        @DisplayName("教师应能看到答案和解析")
        void getQuestionDetail_asTeacher_shouldShowAnswers() throws Exception {
            when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

            Map<String, Object> detail = questionService.getQuestionDetail(questionId, teacherUser);

            assertTrue(detail.containsKey("correct_answers"));
            assertTrue(detail.containsKey("explanation"));
            assertEquals("This is the explanation.", detail.get("explanation"));
        }

        @Test
        @DisplayName("学生不应看到答案和解析")
        void getQuestionDetail_asStudent_shouldHideAnswers() {
            when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

            Map<String, Object> detail = questionService.getQuestionDetail(questionId, studentUser);
            
            assertFalse(detail.containsKey("correct_answers"));
            assertFalse(detail.containsKey("explanation"));
        }

        @Test
        @DisplayName("题目不存在时应抛出 ResourceNotFoundException")
        void getQuestionDetail_notFound_shouldThrowException() {
            when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> questionService.getQuestionDetail(questionId, teacherUser));
        }
    }

    @Nested
    @DisplayName("updateQuestion 方法测试")
    class UpdateQuestionTests {
        @Test
        @DisplayName("成功更新题目")
        void updateQuestion_success() {
            when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            Map<String, Object> data = Map.of("content", "New Content");

            questionService.updateQuestion(questionId, data, teacherUser);

            ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
            verify(questionRepository).save(captor.capture());
            assertEquals("New Content", captor.getValue().getContent());
            assertNotNull(captor.getValue().getUpdatedAt());
        }

        @Test
        @DisplayName("教师修改他人题目时应抛出 BusinessException")
        void updateQuestion_permissionDenied_shouldThrowException() {
            when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
            
            assertThrows(BusinessException.class, () -> questionService.updateQuestion(questionId, Collections.emptyMap(), otherTeacherUser));
        }
    }
    
    @Nested
    @DisplayName("deleteQuestion 方法测试")
    class DeleteQuestionTests {
        @Test
        @DisplayName("成功删除题目")
        void deleteQuestion_success() {
            when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
            
            questionService.deleteQuestion(questionId, teacherUser);
            
            verify(questionRepository).delete(question);
        }

        @Test
        @DisplayName("删除正在使用的题目时应抛出 BusinessException")
        void deleteQuestion_inUse_shouldThrowException() {
            question.setUsageCount(5);
            when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
            
            BusinessException ex = assertThrows(BusinessException.class, () -> questionService.deleteQuestion(questionId, teacherUser));
            assertEquals("题目正在被使用，无法删除", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getQuestionStatistics 方法测试")
    class GetQuestionStatisticsTests {
        @Test
        @DisplayName("应为教师正确计算其题目的统计数据")
        void getQuestionStatistics_forTeacher_shouldCalculateCorrectly() {
            // Arrange
            Question q1 = new Question(); q1.setType("single"); q1.setDifficulty("easy"); q1.setCourseId(101L);
            Question q2 = new Question(); q2.setType("single"); q2.setDifficulty("medium"); q2.setCourseId(101L);
            Question q3 = new Question(); q3.setType("multiple"); q3.setDifficulty("hard"); q3.setCourseId(102L);
            List<Question> questions = List.of(q1, q2, q3);

            Course c1 = new Course(); c1.setName("Java");
            Course c2 = new Course(); c2.setName("Python");
            
            when(questionRepository.findByCreatedBy(teacherUser.getId())).thenReturn(questions);
            when(courseRepository.findById(101L)).thenReturn(Optional.of(c1));
            when(courseRepository.findById(102L)).thenReturn(Optional.of(c2));
            
            // Act
            Map<String, Object> stats = questionService.getQuestionStatistics(teacherUser);

            // Assert
            assertEquals(3, stats.get("total_questions"));
            // V-- 修正后的代码
            assertEquals(1L, ((Map<String, Long>) stats.get("by_difficulty")).get("easy"));
            assertEquals(2.0, (Double) stats.get("avg_difficulty"), 0.01);
            
            List<Map<String,Object>> courseDist = (List<Map<String, Object>>) stats.get("course_distribution");
            assertEquals(2, courseDist.size());
            Map<String, Object> javaStats = courseDist.stream().filter(m -> m.get("course_name").equals("Java")).findFirst().get();
            assertEquals(2L, javaStats.get("question_count"));
        }
    }
}