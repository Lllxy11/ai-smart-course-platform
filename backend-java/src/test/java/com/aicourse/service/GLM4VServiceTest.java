package com.aicourse.service;

import com.aicourse.entity.Course;
import com.aicourse.entity.Question;
import com.aicourse.entity.Submission;
import com.aicourse.entity.Task;
import com.aicourse.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GLM4VServiceTest {

    @Mock
    private RestTemplate restTemplate;
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

    @Spy // 使用Spy来部分模拟自身，以便测试mock/fallback逻辑
    @InjectMocks
    private GLM4VService glm4vService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // 通过setter方法来控制API Key，这是测试这类场景的最佳实践
        // 默认情况下，我们假设API Key是存在的
        glm4vService.setApiKey("test-api-key");
    }

    // =================================================================
    // 1. 测试 API Key 存在时的场景 (模拟外部API调用)
    // =================================================================

    @Test
    @DisplayName("生成题目 - API Key存在且调用成功")
    void generateQuestions_WithApiKey_Success() throws JsonProcessingException {
        // Arrange
        String mockApiResponse = objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of(
                        "message", Map.of("content", "{\"questions\":[{\"id\":1,\"question\":\"Test Question\"}]}")
                ))
        ));
        ResponseEntity<String> successResponse = new ResponseEntity<>(mockApiResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(successResponse);

        // Act
        Map<String, Object> result = glm4vService.generateQuestions("Java", "中等", 1, "选择题");

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(1, result.get("count"));
        List<Map<String, Object>> questions = (List<Map<String, Object>>) result.get("questions");
        assertEquals("Test Question", questions.get(0).get("question"));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("生成题目 - API Key存在但调用失败")
    void generateQuestions_WithApiKey_ApiCallFails() {
        // Arrange
        ResponseEntity<String> errorResponse = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(errorResponse);

        // Act
        Map<String, Object> result = glm4vService.generateQuestions("Java", "中等", 1, "选择题");

        // Assert
        assertTrue((Boolean) result.get("success"));
        // 验证它调用了备用逻辑
        assertEquals("基于数据库现有题目模板生成，建议配置GLM-4V API密钥以获得AI生成的题目", result.get("note"));
    }

    @Test
    @DisplayName("生成题目 - API Key存在但返回格式错误")
    void generateQuestions_WithApiKey_BadFormat() {
        // Arrange
        String badFormatResponse = "{\"choices\":[{\"message\":{\"content\":\"Not a valid JSON\"}}]}";
        ResponseEntity<String> successResponse = new ResponseEntity<>(badFormatResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(successResponse);

        // Act
        Map<String, Object> result = glm4vService.generateQuestions("Java", "中等", 1, "选择题");

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("基于数据库现有题目模板生成，建议配置GLM-4V API密钥以获得AI生成的题目", result.get("note"));
    }


    // =================================================================
    // 2. 测试 API Key 不存在时的场景 (验证 fallback 逻辑)
    // =================================================================

    @Test
    @DisplayName("生成题目 - API Key不存在，调用mock逻辑")
    void generateQuestions_NoApiKey_ShouldUseMock() {
        // Arrange
        glm4vService.setApiKey(null); // 模拟API Key不存在
        when(questionRepository.findAll()).thenReturn(Collections.emptyList()); // 模拟数据库也没有数据

        // Act
        Map<String, Object> result = glm4vService.generateQuestions("Python", "简单", 2, "填空题");

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("count"));
        assertEquals("基于数据库现有题目模板生成，建议配置GLM-4V API密钥以获得AI生成的题目", result.get("note"));
        // 验证没有调用外部API
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("推荐学习路径 - API Key不存在，调用mock逻辑")
    void recommendLearningPath_NoApiKey_ShouldUseMock() {
        // Arrange
        glm4vService.setApiKey(""); // 模拟API Key为空
        Course course = new Course();
        course.setName("Java 基础");
        course.setDescription("Java 编程入门课程");
        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = glm4vService.recommendLearningPath("Java", "beginner", "掌握基础");

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("基于数据库现有课程和任务生成，建议配置GLM-4V API密钥以获得个性化学习路径", result.get("note"));
        List<Map<String, Object>> path = (List<Map<String, Object>>) result.get("learning_path");
        assertFalse(path.isEmpty());
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }
    
    @Test
    @DisplayName("生成摘要 - API Key不存在，调用mock逻辑")
    void generateSummary_NoApiKey_ShouldUseMock() {
        // Arrange
        glm4vService.setApiKey(null);
        String content = "这是一个很长很长的文本，需要被摘要。它包含了很多重要的信息，我们希望AI能够准确地提取出来。";
        when(submissionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = glm4vService.generateSummary(content, 20, "中文");

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertTrue(((String)result.get("summary")).length() <= 20);
        assertEquals("基于内容分析和数据库参考生成，建议配置GLM-4V API密钥以获得AI摘要", result.get("note"));
    }

    @Test
    @DisplayName("检测抄袭 - API Key不存在，调用mock逻辑")
    void detectPlagiarism_NoApiKey_ShouldUseMock() {
        // Arrange
        glm4vService.setApiKey(null);
        String content = "这是一段原创的内容。";
        Submission similarSubmission = new Submission();
        similarSubmission.setContent("这是一段非常相似的内容。");
        when(submissionRepository.findAll()).thenReturn(List.of(similarSubmission));
        
        // Act
        Map<String, Object> result = glm4vService.detectPlagiarism(content);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertTrue(result.containsKey("originality_score"));
        assertEquals("基于数据库内容相似度检测，建议配置GLM-4V API密钥以获得专业抄袭检测", result.get("note"));
    }

    // =================================================================
    // 3. 覆盖更多的备用 (Mock/Fallback) 逻辑分支
    // =================================================================

    @Test
    @DisplayName("Mock生成题目 - 数据库有相关题目")
    void generateMockQuestions_WithDbData() {
        // Arrange
        glm4vService.setApiKey(null);
        Question dbQuestion = new Question();
        dbQuestion.setContent("关于Java，哪个选项是正确的？");
        dbQuestion.setOptions("A. 选项A|B. 选项B|C. 选项C|D. 选项D");
        dbQuestion.setCorrectAnswers("A");
        when(questionRepository.findAll()).thenReturn(List.of(dbQuestion));

        // Act
        Map<String, Object> result = glm4vService.generateQuestions("Java", "困难", 1, "选择题");

        // Assert
        assertTrue((Boolean) result.get("success"));
        List<Map<String, Object>> questions = (List<Map<String, Object>>) result.get("questions");
        assertEquals(1, questions.size());
        // 验证问题内容被变体了
        assertNotEquals("关于Java，哪个选项是正确的？", questions.get(0).get("question"));
    }
    
    @Test
    @DisplayName("Mock学习路径 - 数据库无相关数据")
    void mockRecommendLearningPath_NoDbData() {
        // Arrange
        glm4vService.setApiKey(null);
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = glm4vService.recommendLearningPath("不存在的科目", "beginner", "学习");

        // Assert
        assertTrue((Boolean) result.get("success"));
        List<Map<String, Object>> path = (List<Map<String, Object>>) result.get("learning_path");
        // 验证它生成了基础的2个阶段
        assertEquals(2, path.size());
        assertEquals("基础阶段", path.get(0).get("title"));
    }
    
    // =================================================================
    // 4. 测试 `getUsageStatistics` 及其内部计算方法
    // =================================================================

    @Test
    @DisplayName("获取使用统计 - 数据正常")
    void getUsageStatistics_Success() {
        // Arrange
        when(submissionRepository.count()).thenReturn(100L);
        when(questionRepository.count()).thenReturn(50L);
        when(courseRepository.count()).thenReturn(10L);
        when(taskRepository.count()).thenReturn(20L);
        when(userRepository.countByRole(any())).thenReturn(80L);
        Submission recentSubmission = new Submission();
        recentSubmission.setSubmittedAt(LocalDateTime.now().minusDays(1));
        recentSubmission.setStudentId(1L);
        when(submissionRepository.findAll()).thenReturn(List.of(recentSubmission));

        // Act
        Map<String, Object> stats = glm4vService.getUsageStatistics();

        // Assert
        assertEquals("正常", stats.get("apiStatus"));
        assertTrue((Integer)stats.get("totalQueries") > 0);
        assertTrue((Integer)stats.get("questionsGenerated") > 0);
        assertTrue((Double)stats.get("successRate") > 0);
    }
    
    @Test
    @DisplayName("获取使用统计 - API Key不存在")
    void getUsageStatistics_NoApiKey() {
        // Arrange
        glm4vService.setApiKey(null);

        // Act
        Map<String, Object> stats = glm4vService.getUsageStatistics();
        
        // Assert
        assertEquals("未配置", stats.get("apiStatus"));
        assertEquals(0.0, stats.get("successRate"));
        assertEquals(0, stats.get("averageResponseTime"));
    }
    
    @Test
    @DisplayName("获取使用统计 - 数据库访问异常")
    void getUsageStatistics_DbError() {
        // Arrange
        when(submissionRepository.count()).thenThrow(new RuntimeException("DB connection failed"));
        
        // Act
        Map<String, Object> stats = glm4vService.getUsageStatistics();
        
        // Assert
        assertEquals("错误", stats.get("apiStatus"));
        assertEquals("统计数据获取失败", stats.get("message"));
    }

    // =================================================================
    // 5. 覆盖其他公共方法
    // =================================================================
    
    @Test
    @DisplayName("分析提交内容 - 成功")
    void analyzeSubmission_Success() throws JsonProcessingException {
        // Arrange
        String mockResponseContent = "{\"score\":85,\"feedback\":\"Good job!\"}";
        String mockApiResponse = objectMapper.writeValueAsString(Map.of("choices", List.of(Map.of("message", Map.of("content", mockResponseContent)))));
        ResponseEntity<String> successResponse = new ResponseEntity<>(mockApiResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(successResponse);
        
        // Act
        Map<String, Object> result = glm4vService.analyzeSubmission("学生答案", "问题", "标准答案");
        
        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(85, result.get("score"));
    }

    @Test
    @DisplayName("与AI对话 - 成功")
    void chatWithAI_Success() throws JsonProcessingException {
        // Arrange
        String mockResponseContent = "这是AI的回答。";
        String mockApiResponse = objectMapper.writeValueAsString(Map.of("choices", List.of(Map.of("message", Map.of("content", mockResponseContent)))));
        ResponseEntity<String> successResponse = new ResponseEntity<>(mockApiResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn(successResponse);

        // Act
        Map<String, Object> result = glm4vService.chatWithAI("你好", null, false);
        
        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("这是AI的回答。", result.get("reply"));
    }
    
    @Test
    @DisplayName("与AI对话 - API Key不存在")
    void chatWithAI_NoApiKey() {
        // Arrange
        glm4vService.setApiKey(null);
        
        // Act
        Map<String, Object> result = glm4vService.chatWithAI("你好", null, false);
        
        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("API密钥未配置", result.get("error"));
    }
}