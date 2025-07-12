package com.aicourse.service;

import com.aicourse.entity.*;
import com.aicourse.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GLM4VService Unit Tests")
class GLM4VServiceTest {

    @Mock
    private RestTemplate restTemplate;
    @Spy
    private ObjectMapper objectMapper; // Use @Spy to allow both mocking and real method calls
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
        // Use ReflectionTestUtils to set private @Value fields
        ReflectionTestUtils.setField(glm4vService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(glm4vService, "baseUrl", "https://test.com");
        ReflectionTestUtils.setField(glm4vService, "model", "test-model");
    }

    // =================== generateQuestions Tests ===================

    @Test
    @DisplayName("generateQuestions - Success with API Key")
    void generateQuestions_successWithApiKey() throws Exception {
        mockAiSuccessResponse("{\"questions\":[{\"id\":1}]}");
        Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");
        assertTrue((Boolean) result.get("success"));
        assertEquals(1, result.get("count"));
    }

    @Test
    @DisplayName("generateQuestions - No API Key, uses mock with existing questions")
    void generateQuestions_noApiKeyUsesMockWithDbQuestions() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        Question mockQuestion = new Question();
        mockQuestion.setContent("What is Java?");
        mockQuestion.setOptions("A|B|C|D");
        when(questionRepository.findAll()).thenReturn(List.of(mockQuestion));
        Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");
        assertTrue((Boolean) result.get("success"));
        assertEquals("基于数据库现有题目模板生成，建议配置GLM-4V API密钥以获得AI生成的题目", result.get("note"));
    }
    
    @Test
    @DisplayName("generateQuestions - No API Key, uses mock with no existing questions")
    void generateQuestions_noApiKeyUsesMockWithoutDbQuestions() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        when(questionRepository.findAll()).thenReturn(Collections.emptyList());
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());
        Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");
        assertTrue((Boolean) result.get("success"));
        assertEquals(1, result.get("count"));
    }

    @Test
    @DisplayName("generateQuestions - AI call fails, uses mock")
    void generateQuestions_aiFailsUsesMock() {
        mockAiFailure();
        when(questionRepository.findAll()).thenReturn(Collections.emptyList());
        Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("generateQuestions - Malformed AI response, uses mock")
    void generateQuestions_malformedResponseUsesMock() throws Exception {
        mockAiSuccessResponse("not a json");
        when(questionRepository.findAll()).thenReturn(Collections.emptyList());
        Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");
        assertTrue((Boolean) result.get("success"));
    }
    
    @Test
    @DisplayName("generateQuestions - Main try-catch block")
    void generateQuestions_mainTryCatch() {
        when(questionRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        Map<String, Object> result = glm4vService.generateQuestions("Java", "Easy", 1, "Multiple Choice");
        assertTrue((Boolean) result.get("success"));
        assertEquals(1, ((List<?>)result.get("questions")).size()); // Fallback to basic mock
    }

    // =================== analyzeSubmission Tests ===================

    @Test
    @DisplayName("analyzeSubmission - Success")
    void analyzeSubmission_success() throws Exception {
        mockAiSuccessResponse("{\"score\":85}");
        Map<String, Object> result = glm4vService.analyzeSubmission("content", "question", "answer");
        assertTrue((Boolean) result.get("success"));
        assertEquals(85, result.get("score"));
    }

    @Test
    @DisplayName("analyzeSubmission - AI call fails")
    void analyzeSubmission_aiFails() {
        mockAiFailure();
        Map<String, Object> result = glm4vService.analyzeSubmission("content", "question", null);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("analyzeSubmission - Malformed AI response")
    void analyzeSubmission_malformedResponse() throws Exception {
        mockAiSuccessResponse("not a json");
        Map<String, Object> result = glm4vService.analyzeSubmission("content", "question", "answer");
        assertFalse((Boolean) result.get("success"));
        assertEquals("AI返回格式错误", result.get("error"));
    }
    
    @Test
    @DisplayName("analyzeSubmission - Main try-catch block")
    void analyzeSubmission_mainTryCatch() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RuntimeException("Network Error"));
        Map<String, Object> result = glm4vService.analyzeSubmission("content", "q", "a");
        assertFalse((Boolean) result.get("success"));
        assertEquals("Network Error", result.get("error"));
    }

    // =================== recommendLearningPath Tests ===================

    @Test
    @DisplayName("recommendLearningPath - Success with API Key")
    void recommendLearningPath_successWithApiKey() throws Exception {
        mockAiSuccessResponse("{\"learning_path\":[]}");
        Map<String, Object> result = glm4vService.recommendLearningPath("Math", "Beginner", "Calculus");
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("recommendLearningPath - No API Key, uses mock with DB data")
    void recommendLearningPath_noApiKeyUsesMockWithDbData() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", null);
        Course course = new Course();
        course.setName("Java Basics");
        course.setDescription("Intro to Java");
        Task task = new Task();
        task.setTitle("Java Hello World");
        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(taskRepository.findAll()).thenReturn(List.of(task));
        Map<String, Object> result = glm4vService.recommendLearningPath("Java", "Beginner", "Get Started");
        assertTrue((Boolean) result.get("success"));
        assertEquals("基于数据库现有课程和任务生成，建议配置GLM-4V API密钥以获得个性化学习路径", result.get("note"));
    }
    
    @Test
    @DisplayName("recommendLearningPath - No API Key, uses mock without DB data")
    void recommendLearningPath_noApiKeyUsesMockWithoutDbData() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", null);
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());
        Map<String, Object> result = glm4vService.recommendLearningPath("Java", "Beginner", "Get Started");
        assertTrue((Boolean) result.get("success"));
        assertEquals(2, ((List<?>)result.get("learning_path")).size());
    }
    
    @Test
    @DisplayName("recommendLearningPath - Main try-catch block")
    void recommendLearningPath_mainTryCatch() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", null);
        when(courseRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        Map<String, Object> result = glm4vService.recommendLearningPath("Java", "Beginner", "Get Started");
        assertTrue((Boolean) result.get("success"));
        assertEquals("基础学习路径模板", result.get("note"));
    }

    // =================== generateSummary Tests ===================

    @Test
    @DisplayName("generateSummary - Success with API Key")
    void generateSummary_successWithApiKey() throws Exception {
        mockAiSuccessResponse("This is a summary.");
        Map<String, Object> result = glm4vService.generateSummary("This is the original long content.", 50, "English");
        assertTrue((Boolean) result.get("success"));
        assertEquals("This is a summary.", result.get("summary"));
    }

    @Test
    @DisplayName("generateSummary - No API Key, uses mock")
    void generateSummary_noApiKeyUsesMock() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        when(submissionRepository.findAll()).thenReturn(Collections.emptyList());
        Map<String, Object> result = glm4vService.generateSummary("This is the original long content.", 10, "English");
        assertTrue((Boolean) result.get("success"));
        assertEquals("This is th...", result.get("summary"));
    }
    
    @Test
    @DisplayName("generateSummary - Empty content")
    void generateSummary_emptyContent() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        Map<String, Object> result = glm4vService.generateSummary("", 10, "English");
        assertEquals("内容为空，无法生成摘要", result.get("summary"));
    }
    
    @Test
    @DisplayName("generateSummary - Main try-catch block")
    void generateSummary_mainTryCatch() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        when(submissionRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        Map<String, Object> result = glm4vService.generateSummary("Some content", 10, "English");
        assertEquals("Some conte...", result.get("summary"));
    }

    // =================== detectPlagiarism Tests ===================

    @Test
    @DisplayName("detectPlagiarism - Success with API Key")
    void detectPlagiarism_successWithApiKey() throws Exception {
        mockAiSuccessResponse("{\"originality_score\":95}");
        Map<String, Object> result = glm4vService.detectPlagiarism("unique content");
        assertTrue((Boolean) result.get("success"));
        assertEquals(95, result.get("originality_score"));
    }

    @Test
    @DisplayName("detectPlagiarism - No API Key, uses mock with different similarities")
    void detectPlagiarism_noApiKeyUsesMock() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        Submission s1 = new Submission(); s1.setContent("This is a highly similar text");
        Submission s2 = new Submission(); s2.setContent("This is a moderately similar text");
        Submission s3 = new Submission(); s3.setContent("This is a slightly similar text");
        Submission s4 = new Submission(); s4.setContent("Totally different words here");
        when(submissionRepository.findAll()).thenReturn(List.of(s1, s2, s3, s4));
        
        // Test high similarity
        glm4vService.detectPlagiarism("This is a highly similar text, almost identical");
        // Test medium similarity
        glm4vService.detectPlagiarism("This text is moderately similar");
        // Test low similarity
        glm4vService.detectPlagiarism("A slightly similar text");
        // Test no similarity
        glm4vService.detectPlagiarism("Completely original content");
    }
    
    @Test
    @DisplayName("detectPlagiarism - Main try-catch block")
    void detectPlagiarism_mainTryCatch() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        when(submissionRepository.findAll()).thenThrow(new RuntimeException("DB Error"));
        Map<String, Object> result = glm4vService.detectPlagiarism("some content");
        assertTrue((Integer)result.get("originality_score") >= 85);
    }

    // =================== generateCourseContent Tests ===================

    @Test
    @DisplayName("generateCourseContent - Success with API Key")
    void generateCourseContent_successWithApiKey() throws Exception {
        mockAiSuccessResponse("{\"objectives\":[]}");
        Map<String, Object> result = glm4vService.generateCourseContent("AI", "Intro", "Beginner");
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    @DisplayName("generateCourseContent - No API Key, uses mock for Java, Python, and Generic")
    void generateCourseContent_noApiKeyUsesMocks() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        // Java
        Map<String, Object> javaResult = glm4vService.generateCourseContent("Java Programming", "Intro", "Beginner");
        assertTrue((Boolean) javaResult.get("success"));
        // Python
        Map<String, Object> pythonResult = glm4vService.generateCourseContent("Python Data Science", "Intro", "Beginner");
        assertTrue((Boolean) pythonResult.get("success"));
        // Generic
        Map<String, Object> genericResult = glm4vService.generateCourseContent("History", "Intro", "Beginner");
        assertTrue((Boolean) genericResult.get("success"));
    }

    // =================== chatWithAI Tests ===================

    @Test
    @DisplayName("chatWithAI - Success with and without context")
    void chatWithAI_success() throws Exception {
        mockAiSuccessResponse("Hello, I am your assistant.");
        // Without context
        Map<String, Object> result1 = glm4vService.chatWithAI("Hi", null, false);
        assertTrue((Boolean) result1.get("success"));
        // With context
        Map<String, Object> result2 = glm4vService.chatWithAI("Help me", List.of(Map.of("type", "subject", "data", "Math")), false);
        assertTrue((Boolean) result2.get("success"));
    }

    @Test
    @DisplayName("chatWithAI - No API Key")
    void chatWithAI_noApiKey() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        Map<String, Object> result = glm4vService.chatWithAI("Hi", null, false);
        assertFalse((Boolean) result.get("success"));
        assertEquals("API密钥未配置", result.get("error"));
    }
    
    @Test
    @DisplayName("chatWithAI - RestTemplate fails")
    void chatWithAI_restTemplateFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RuntimeException("Connection failed"));
        Map<String, Object> result = glm4vService.chatWithAI("Hi", null, false);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String)result.get("error")).contains("Connection failed"));
    }

    // =================== getUsageStatistics Tests ===================

    @Test
    @DisplayName("getUsageStatistics - Success and fallback")
    void getUsageStatistics_successAndFallback() {
        // Success
        when(submissionRepository.count()).thenReturn(100L);
        when(questionRepository.count()).thenReturn(50L);
        when(courseRepository.count()).thenReturn(10L);
        when(taskRepository.count()).thenReturn(20L);
        when(userRepository.countByRole(UserRole.STUDENT)).thenReturn(200L);
        when(userRepository.findAll()).thenReturn(List.of(new User()));
        Map<String, Object> result = glm4vService.getUsageStatistics();
        assertEquals("正常", result.get("apiStatus"));

        // Fallback on exception
        when(submissionRepository.count()).thenThrow(new RuntimeException("DB down"));
        Map<String, Object> fallbackResult = glm4vService.getUsageStatistics();
        assertEquals("错误", fallbackResult.get("apiStatus"));
    }
    
    @Test
    @DisplayName("getUsageStatistics - No API Key")
    void getUsageStatistics_noApiKey() {
        ReflectionTestUtils.setField(glm4vService, "apiKey", "");
        Map<String, Object> result = glm4vService.getUsageStatistics();
        assertEquals("未配置", result.get("apiStatus"));
    }
    
    // =================== Private methods tests (indirectly) ===================
    
    @Test
    @DisplayName("parseJsonResponse - Handles JSON in markdown block and no JSON found")
    void parseJsonResponse_allPaths() throws Exception {
        // Markdown block
        mockAiSuccessResponse("```json\n{\"key\":\"value\"}\n```");
        Map<String, Object> result1 = glm4vService.analyzeSubmission("c", "q", "a");
        assertTrue((Boolean) result1.get("success"));
        assertEquals("value", result1.get("key"));
        
        // No JSON found
        mockAiSuccessResponse("Here is some text without any json.");
        Map<String, Object> result2 = glm4vService.analyzeSubmission("c", "q", "a");
        assertFalse((Boolean) result2.get("success"));
        assertTrue(((String)result2.get("error")).contains("AI返回格式错误"));
    }

    // =================== Helper methods for mocking ===================

    private void mockAiSuccessResponse(String content) throws Exception {
        // This setup mocks the nested structure of the AI response
        String jsonResponseString = "{\"choices\":[{\"message\":{\"content\":\"" + content.replace("\"", "\\\"") + "\"}}]}";
        ResponseEntity<String> successResponse = new ResponseEntity<>(jsonResponseString, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(successResponse);
                
        // Mock the parsing of the final content string
        // This is a bit tricky because the TypeReference is generic. We'll use a lenient mock.
        lenient().when(objectMapper.readValue(eq(content), any(TypeReference.class)))
                .thenReturn(Map.of(
                    "key", "value", 
                    "score", 85, 
                    "originality_score", 95, 
                    "success", true, 
                    "questions", List.of(Map.of("id", 1)), 
                    "learning_path", List.of()
                ));
    }

    private void mockAiFailure() {
        ResponseEntity<String> failureResponse = new ResponseEntity<>("Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(failureResponse);
    }
}
