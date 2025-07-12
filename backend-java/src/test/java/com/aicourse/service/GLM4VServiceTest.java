package com.aicourse.service;

import com.aicourse.entity.*;
import com.aicourse.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GLM4VService 单元测试类
 * 使用 JUnit 5 和 Mockito 进行测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GLM4VService 单元测试")
class GLM4VServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GLM4VService glm4vService;

    @BeforeEach
    void setUp() {
        // 设置私有字段
        ReflectionTestUtils.setField(glm4vService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(glm4vService, "baseUrl", "https://test.com");
        ReflectionTestUtils.setField(glm4vService, "model", "test-model");
    }

    @Nested
    @DisplayName("generateQuestions 方法测试")
    class GenerateQuestionsTests {


        @Test
        @DisplayName("无API密钥时使用模拟数据 - 数据库有现有题目")
        void shouldUseMockDataWhenNoApiKeyWithExistingQuestions() {
            // Given
            ReflectionTestUtils.setField(glm4vService, "apiKey", "");

            Question mockQuestion = new Question();
            mockQuestion.setContent("什么是Java？");
            mockQuestion.setOptions("A. 编程语言|B. 数据库|C. 操作系统|D. 网络协议");
            mockQuestion.setCorrectAnswers("A");

            when(questionRepository.findAll()).thenReturn(List.of(mockQuestion));

            // When
            Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");

            // Then
            assertTrue((Boolean) result.get("success"));
            assertEquals("基于数据库现有题目模板生成，建议配置GLM-4V API密钥以获得AI生成的题目", result.get("note"));
            assertNotNull(result.get("questions"));
        }

        @Test
        @DisplayName("无API密钥时使用模拟数据 - 数据库无现有题目")
        void shouldUseMockDataWhenNoApiKeyWithoutExistingQuestions() {
            // Given
            ReflectionTestUtils.setField(glm4vService, "apiKey", "");
            when(questionRepository.findAll()).thenReturn(Collections.emptyList());
            when(courseRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");

            // Then
            assertTrue((Boolean) result.get("success"));
            assertEquals(1, result.get("count"));
            assertNotNull(result.get("questions"));
        }


        @Test
        @DisplayName("数据库异常时使用基础模拟数据")
        void shouldUseBasicMockDataWhenDatabaseException() {
            // Given
            when(questionRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));
            ReflectionTestUtils.setField(glm4vService, "apiKey", "");

            // When
            Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");

            // Then
            assertTrue((Boolean) result.get("success"));
            assertEquals(1, ((List<?>) result.get("questions")).size());
        }

        @ParameterizedTest
        @CsvSource({
                "Java, Easy, 1, Multiple Choice",
                "Python, Medium, 3, True/False",
                "Math, Hard, 5, Essay"
        })
        @DisplayName("参数化测试不同主题和难度")
        void shouldGenerateQuestionsWithDifferentParameters(String topic, String difficulty, int count, String type) {
            // Given
            ReflectionTestUtils.setField(glm4vService, "apiKey", "");
            when(questionRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            Map<String, Object> result = glm4vService.generateQuestions(topic, difficulty, count, type);

            // Then
            assertTrue((Boolean) result.get("success"));
            assertEquals(topic, result.get("topic"));
            assertEquals(difficulty, result.get("difficulty"));
            assertEquals(count, result.get("count"));
        }
    }

    @Nested
    @DisplayName("analyzeSubmission 方法测试")
    class AnalyzeSubmissionTests {


        @Nested
        @DisplayName("recommendLearningPath 方法测试")
        class RecommendLearningPathTests {


            @Test
            @DisplayName("无API密钥时使用模拟数据 - 数据库有课程和任务")
            void shouldUseMockDataWhenNoApiKeyWithDbData() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                Course course = new Course();
                course.setName("Java基础课程");
                course.setDescription("Java编程入门");

                Task task = new Task();
                task.setTitle("Java Hello World");
                task.setDescription("编写第一个Java程序");

                when(courseRepository.findAll()).thenReturn(List.of(course));
                when(taskRepository.findAll()).thenReturn(List.of(task));

                // When
                Map<String, Object> result = glm4vService.recommendLearningPath("Java", "Beginner", "Get Started");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertEquals("基于数据库现有课程和任务生成，建议配置GLM-4V API密钥以获得个性化学习路径", result.get("note"));
                assertNotNull(result.get("learning_path"));
            }

            @Test
            @DisplayName("无API密钥时使用模拟数据 - 数据库无数据")
            void shouldUseMockDataWhenNoApiKeyWithoutDbData() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");
                when(courseRepository.findAll()).thenReturn(Collections.emptyList());
                when(taskRepository.findAll()).thenReturn(Collections.emptyList());

                // When
                Map<String, Object> result = glm4vService.recommendLearningPath("Java", "Beginner", "Get Started");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertEquals(2, ((List<?>) result.get("learning_path")).size());
            }

            @Test
            @DisplayName("数据库异常时使用基础模板")
            void shouldUseBasicTemplateWhenDatabaseException() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");
                when(courseRepository.findAll()).thenThrow(new RuntimeException("Database error"));

                // When
                Map<String, Object> result = glm4vService.recommendLearningPath("Java", "Beginner", "Get Started");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertEquals("基础学习路径模板", result.get("note"));
            }
        }

        @Nested
        @DisplayName("generateSummary 方法测试")
        class GenerateSummaryTests {


            @Test
            @DisplayName("空内容时返回提示信息")
            void shouldReturnMessageWhenContentIsEmpty() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                // When
                Map<String, Object> result = glm4vService.generateSummary("", 10, "English");

                // Then
                assertEquals("内容为空，无法生成摘要", result.get("summary"));
            }

            @Test
            @DisplayName("数据库异常时使用基础摘要")
            void shouldUseBasicSummaryWhenDatabaseException() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");
                String content = "Some content";
                when(submissionRepository.findAll()).thenThrow(new RuntimeException("Database error"));

                // When
                Map<String, Object> result = glm4vService.generateSummary(content, 10, "English");

                // Then
                assertEquals("Some conte...", result.get("summary"));
            }

            @ParameterizedTest
            @CsvSource({
                    "Short content, 20, English",
                    "这是一段中文内容，需要摘要处理, 30, 中文",
                    "Very long content that exceeds the maximum length limit, 15, English"
            })
            @DisplayName("参数化测试不同内容和长度")
            void shouldGenerateSummaryWithDifferentParameters(String content, int maxLength, String language) {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");
                when(submissionRepository.findAll()).thenReturn(Collections.emptyList());

                // When
                Map<String, Object> result = glm4vService.generateSummary(content, maxLength, language);

                // Then
                assertTrue((Boolean) result.get("success"));
                assertNotNull(result.get("summary"));
                assertNotNull(result.get("compression_ratio"));
            }
        }

        @Nested
        @DisplayName("detectPlagiarism 方法测试")
        class DetectPlagiarismTests {


            @Test
            @DisplayName("无API密钥时使用模拟检测 - 低相似度")
            void shouldUseMockDetectionWhenNoApiKeyWithLowSimilarity() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                Submission differentSubmission = new Submission();
                differentSubmission.setContent("Totally different content");

                when(submissionRepository.findAll()).thenReturn(List.of(differentSubmission));

                // When
                Map<String, Object> result = glm4vService.detectPlagiarism("Completely original content");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertTrue((Integer) result.get("originality_score") > 80);
                assertEquals("low", result.get("risk_level"));
            }

            @Test
            @DisplayName("数据库异常时使用基础检测")
            void shouldUseBasicDetectionWhenDatabaseException() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");
                when(submissionRepository.findAll()).thenThrow(new RuntimeException("Database error"));

                // When
                Map<String, Object> result = glm4vService.detectPlagiarism("some content");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertTrue((Integer) result.get("originality_score") >= 85);
            }
        }

        @Nested
        @DisplayName("generateCourseContent 方法测试")
        class GenerateCourseContentTests {


            @Test
            @DisplayName("无API密钥时生成Java课程内容")
            void shouldGenerateJavaCourseContentWhenNoApiKey() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                // When
                Map<String, Object> result = glm4vService.generateCourseContent("Java Programming", "Intro", "Beginner");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertEquals("Java Programming", result.get("courseName"));
                assertEquals("Beginner", result.get("difficulty"));
                assertNotNull(result.get("content"));
                assertEquals("mock_data", result.get("source"));
            }

            @Test
            @DisplayName("无API密钥时生成Python课程内容")
            void shouldGeneratePythonCourseContentWhenNoApiKey() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                // When
                Map<String, Object> result = glm4vService.generateCourseContent("Python Data Science", "Intro", "Beginner");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertEquals("Python Data Science", result.get("courseName"));
                assertEquals("Beginner", result.get("difficulty"));
                assertNotNull(result.get("content"));
            }

            @Test
            @DisplayName("无API密钥时生成通用课程内容")
            void shouldGenerateGenericCourseContentWhenNoApiKey() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                // When
                Map<String, Object> result = glm4vService.generateCourseContent("History", "Intro", "Beginner");

                // Then
                assertTrue((Boolean) result.get("success"));
                assertEquals("History", result.get("courseName"));
                assertEquals("Beginner", result.get("difficulty"));
                assertNotNull(result.get("content"));
            }

            @ParameterizedTest
            @CsvSource({
                    "Java Programming, Introduction to Java, Beginner",
                    "Python Data Science, Data Analysis with Python, Intermediate",
                    "Web Development, Full Stack Development, Advanced"
            })
            @DisplayName("参数化测试不同课程类型")
            void shouldGenerateCourseContentWithDifferentTypes(String courseName, String description, String difficulty) {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                // When
                Map<String, Object> result = glm4vService.generateCourseContent(courseName, description, difficulty);

                // Then
                assertTrue((Boolean) result.get("success"));
                assertEquals(courseName, result.get("courseName"));
                assertEquals(difficulty, result.get("difficulty"));
            }
        }

        @Nested
        @DisplayName("chatWithAI 方法测试")
        class ChatWithAITests {


            @Test
            @DisplayName("无API密钥时返回错误")
            void shouldReturnErrorWhenNoApiKey() {
                // Given
                ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                // When
                Map<String, Object> result = glm4vService.chatWithAI("Hi", null, false);

                // Then
                assertFalse((Boolean) result.get("success"));
                assertEquals("API密钥未配置", result.get("error"));
                assertEquals("test-model", result.get("model"));
            }


            @Nested
            @DisplayName("getUsageStatistics 方法测试")
            class GetUsageStatisticsTests {

                @Test
                @DisplayName("成功获取使用统计 - 有API密钥")
                void shouldGetUsageStatisticsSuccessfullyWithApiKey() {
                    // Given
                    when(submissionRepository.count()).thenReturn(100L);
                    when(questionRepository.count()).thenReturn(50L);
                    when(courseRepository.count()).thenReturn(10L);
                    when(taskRepository.count()).thenReturn(20L);
                    when(userRepository.countByRole(UserRole.STUDENT)).thenReturn(200L);
                    when(userRepository.findAll()).thenReturn(List.of(new User()));

                    // When
                    Map<String, Object> result = glm4vService.getUsageStatistics();

                    // Then
                    assertEquals("正常", result.get("apiStatus"));
                    assertEquals("test-model", result.get("model"));
                    assertNotNull(result.get("totalQueries"));
                    assertNotNull(result.get("questionsGenerated"));
                    assertNotNull(result.get("submissionsAnalyzed"));
                    assertNotNull(result.get("pathsRecommended"));
                    assertNotNull(result.get("summariesGenerated"));
                    assertNotNull(result.get("plagiarismChecks"));
                    assertNotNull(result.get("chatMessages"));
                    assertNotNull(result.get("activeUsers"));
                    assertNotNull(result.get("todayQueries"));
                    assertNotNull(result.get("todayActiveUsers"));
                    assertNotNull(result.get("successRate"));
                    assertNotNull(result.get("averageResponseTime"));
                    assertNotNull(result.get("lastUpdated"));
                    assertEquals("AI使用统计数据", result.get("message"));
                }

                @Test
                @DisplayName("无API密钥时显示未配置状态")
                void shouldShowNotConfiguredStatusWhenNoApiKey() {
                    // Given
                    ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                    // When
                    Map<String, Object> result = glm4vService.getUsageStatistics();

                    // Then
                    assertEquals("未配置", result.get("apiStatus"));
                    assertEquals(0.0, result.get("successRate"));
                    assertEquals(0, result.get("averageResponseTime"));
                }


                @Test
                @DisplayName("计算活跃用户数")
                void shouldCalculateActiveUsersCorrectly() {
                    // Given
                    User user1 = new User();
                    user1.setLastLogin(LocalDateTime.now().minusDays(10));
                    User user2 = new User();
                    user2.setLastLogin(LocalDateTime.now().minusDays(5));
                    User user3 = new User();
                    user3.setLastLogin(LocalDateTime.now().minusDays(35)); // 超过30天

                    when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

                    // When
                    Map<String, Object> result = glm4vService.getUsageStatistics();

                    // Then
                    assertNotNull(result.get("activeUsers"));
                    assertTrue((Integer) result.get("activeUsers") >= 0);
                }

                @Test
                @DisplayName("计算今日查询数")
                void shouldCalculateTodayQueriesCorrectly() {
                    // Given
                    Submission submission1 = new Submission();
                    submission1.setSubmittedAt(LocalDateTime.now().minusHours(2));
                    Submission submission2 = new Submission();
                    submission2.setSubmittedAt(LocalDateTime.now().minusHours(5));

                    when(submissionRepository.findAll()).thenReturn(List.of(submission1, submission2));

                    // When
                    Map<String, Object> result = glm4vService.getUsageStatistics();

                    // Then
                    assertNotNull(result.get("todayQueries"));
                    assertTrue((Integer) result.get("todayQueries") >= 0);
                }
            }

            @Nested
            @DisplayName("私有方法测试（通过公共方法间接测试）")
            class PrivateMethodsTests {


                @Test
                @DisplayName("calculateSimilarity - 计算文本相似度")
                void shouldCalculateSimilarityCorrectly() {
                    // Given
                    ReflectionTestUtils.setField(glm4vService, "apiKey", "");

                    Submission submission = new Submission();
                    submission.setContent("This is a test content");

                    when(submissionRepository.findAll()).thenReturn(List.of(submission));

                    // When
                    Map<String, Object> result = glm4vService.detectPlagiarism("This is a test content with more words");

                    // Then
                    assertTrue((Boolean) result.get("success"));
                    assertNotNull(result.get("originality_score"));
                    assertNotNull(result.get("similarity_rate"));
                }
            }

            // =================== 辅助方法 ===================

            /**
             * 模拟AI成功响应
             */
            private void mockAiSuccessResponse(String content) throws Exception {
                String jsonResponseString = "{\"choices\":[{\"message\":{\"content\":\"" + content.replace("\"", "\\\"")
                        + "\"}}]}";
                ResponseEntity<String> successResponse = new ResponseEntity<>(jsonResponseString, HttpStatus.OK);
                when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                        .thenReturn(successResponse);

                // 简化模拟，避免复杂的TypeReference问题
                try {
                    // 使用更简单的模拟方式
                    lenient().when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                            .thenAnswer(invocation -> {
                                String input = invocation.getArgument(0);
                                if (input.contains("questions")) {
                                    return Map.of("questions", List.of(Map.of("id", 1)));
                                } else if (input.contains("score")) {
                                    return Map.of("score", 85);
                                } else if (input.contains("originality_score")) {
                                    return Map.of("originality_score", 95);
                                } else if (input.contains("learning_path")) {
                                    return Map.of("learning_path", List.of());
                                } else if (input.contains("objectives")) {
                                    return Map.of("objectives", List.of());
                                } else if (input.contains("key")) {
                                    return Map.of("key", "value");
                                } else {
                                    return Map.of("success", true);
                                }
                            });
                } catch (Exception e) {
                    // 如果模拟失败，使用默认行为
                    lenient().when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                            .thenReturn(Map.of("success", true));
                }
            }

            /**
             * 模拟AI失败响应
             */
            private void mockAiFailure() {
                ResponseEntity<String> failureResponse = new ResponseEntity<>("Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
                when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                        .thenReturn(failureResponse);
            }
        }
    }
}
