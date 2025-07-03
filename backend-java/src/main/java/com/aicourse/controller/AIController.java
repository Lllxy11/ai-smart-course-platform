package com.aicourse.controller;

import com.aicourse.dto.AI.ChatRequest;
import com.aicourse.dto.AI.QuestionGenerationRequest;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.service.GLM4VService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI功能控制器
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI功能", description = "AI智能功能API接口")
public class AIController {

    @Autowired
    private GLM4VService glm4vService;

    @PostMapping("/generate-questions")
    @Operation(summary = "AI生成题目", description = "使用AI生成各种类型的题目（仅教师和管理员）")
    public ResponseEntity<Map<String, Object>> generateQuestions(
            @Parameter(description = "题目生成请求") @Valid @RequestBody QuestionGenerationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        // 权限检查
        if (currentUser.getRole() != UserRole.TEACHER && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException("无权使用AI生成功能");
        }

        Map<String, Object> result = glm4vService.generateQuestions(
                request.getTopic(),
                request.getDifficulty(),
                request.getCount(),
                request.getQuestionType()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze-submission")
    @Operation(summary = "AI分析提交内容", description = "使用AI分析学生提交的作业内容（仅教师和管理员）")
    public ResponseEntity<Map<String, Object>> analyzeSubmission(
            @Parameter(description = "提交内容分析请求") @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal User currentUser
    ) {
        // 权限检查
        if (currentUser.getRole() != UserRole.TEACHER && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException("无权使用AI分析功能");
        }

        String submissionContent = (String) request.get("submissionContent");
        String question = (String) request.get("question");
        String correctAnswer = (String) request.get("correctAnswer");

        Map<String, Object> result = glm4vService.analyzeSubmission(
                submissionContent, question, correctAnswer
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/recommend-learning-path")
    @Operation(summary = "AI推荐学习路径", description = "使用AI为学生推荐个性化学习路径")
    public ResponseEntity<Map<String, Object>> recommendLearningPath(
            @Parameter(description = "学习路径推荐请求") @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal User currentUser
    ) {
        Long userId = Long.valueOf(request.get("userId").toString());
        
        // 权限检查：学生只能查看自己的学习路径
        if (currentUser.getRole() == UserRole.STUDENT && !currentUser.getId().equals(userId)) {
            throw new BusinessException("只能查看自己的学习路径");
        }

        String subject = (String) request.get("subject");
        String currentLevel = (String) request.getOrDefault("currentLevel", "beginner");
        String learningGoals = (String) request.get("learningGoals");

        Map<String, Object> result = glm4vService.recommendLearningPath(
                subject, currentLevel, learningGoals
        );

        // 添加用户ID到结果中
        result.put("userId", userId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-course-content")
    @Operation(summary = "AI生成课程内容", description = "使用AI根据课程名称自动生成学习内容")
    public ResponseEntity<Map<String, Object>> generateCourseContent(
            @Parameter(description = "课程内容生成请求") @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal User currentUser
    ) {
        // 所有已认证用户都可以查看课程内容
        // 教师和管理员可以生成新内容，学生可以查看现有内容
        
        String courseName = (String) request.get("courseName");
        String courseDescription = (String) request.getOrDefault("courseDescription", "");
        String difficulty = (String) request.getOrDefault("difficulty", "intermediate");

        if (courseName == null || courseName.trim().isEmpty()) {
            throw new BusinessException("课程名称不能为空");
        }

        Map<String, Object> result = glm4vService.generateCourseContent(
                courseName, courseDescription, difficulty
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-summary")
    @Operation(summary = "AI生成内容摘要", description = "使用AI为给定内容生成摘要")
    public ResponseEntity<Map<String, Object>> generateSummary(
            @Parameter(description = "摘要生成请求") @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal User currentUser
    ) {
        String content = (String) request.get("content");
        Integer maxLength = (Integer) request.getOrDefault("maxLength", 200);
        String language = (String) request.getOrDefault("language", "chinese");

        Map<String, Object> result = glm4vService.generateSummary(
                content, maxLength, language
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/detect-plagiarism")
    @Operation(summary = "AI检测抄袭", description = "使用AI检测内容是否存在抄袭（仅教师和管理员）")
    public ResponseEntity<Map<String, Object>> detectPlagiarism(
            @Parameter(description = "抄袭检测请求") @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User currentUser
    ) {
        // 权限检查
        if (currentUser.getRole() != UserRole.TEACHER && currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException("无权使用抄袭检测功能");
        }

        String content = request.get("content");

        Map<String, Object> result = glm4vService.detectPlagiarism(content);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/chat")
    @Operation(summary = "与AI对话", description = "与AI进行智能对话交互")
    public ResponseEntity<Map<String, Object>> chatWithAI(
            @Parameter(description = "AI对话请求") @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> result = glm4vService.chatWithAI(
                request.getMessage(),
                request.getContext(),
                request.getUseReasoner()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/features")
    @Operation(summary = "获取AI功能列表", description = "获取当前用户可用的AI功能列表")
    public ResponseEntity<Map<String, Object>> getAIFeatures(
            @AuthenticationPrincipal User currentUser
    ) {
        boolean isTeacherOrAdmin = currentUser.getRole() == UserRole.TEACHER || currentUser.getRole() == UserRole.ADMIN;

        Map<String, Object> features = Map.of(
                "generateQuestions", Map.of(
                        "available", isTeacherOrAdmin,
                        "description", "AI智能生成各种类型题目"
                ),
                "analyzeSubmission", Map.of(
                        "available", isTeacherOrAdmin,
                        "description", "AI分析学生作业提交内容"
                ),
                "recommendLearningPath", Map.of(
                        "available", true,
                        "description", "AI推荐个性化学习路径"
                ),
                "generateCourseContent", Map.of(
                        "available", true,
                        "description", "AI生成课程学习内容"
                ),
                "generateSummary", Map.of(
                        "available", true,
                        "description", "AI生成内容摘要"
                ),
                "detectPlagiarism", Map.of(
                        "available", isTeacherOrAdmin,
                        "description", "AI检测内容抄袭"
                ),
                "chat", Map.of(
                        "available", true,
                        "description", "与AI智能助手对话"
                )
        );

        return ResponseEntity.ok(Map.of(
                "features", features,
                "userRole", currentUser.getRole().name()
        ));
    }

    @GetMapping("/usage-stats")
    @Operation(summary = "获取AI使用统计", description = "获取AI功能使用统计信息（仅管理员）")
    public ResponseEntity<Map<String, Object>> getAIUsageStats(
            @AuthenticationPrincipal User currentUser
    ) {
        // 权限检查
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 获取真实的AI使用统计数据
        Map<String, Object> stats = glm4vService.getUsageStatistics();

        return ResponseEntity.ok(stats);
    }
} 