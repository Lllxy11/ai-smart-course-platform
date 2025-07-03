package com.aicourse.controller;

import com.aicourse.entity.Question;
import com.aicourse.entity.User;
import com.aicourse.exception.BusinessException;
import com.aicourse.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getQuestions(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser) {

        try {
            Pageable pageable = PageRequest.of(skip / limit, limit);
            Page<Question> questionPage = questionService.getQuestions(
                    pageable, questionType, difficulty, courseId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("questions", questionPage.getContent());
            response.put("total", questionPage.getTotalElements());
            response.put("page", questionPage.getNumber());
            response.put("pages", questionPage.getTotalPages());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("获取题目列表失败: " + e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createQuestion(
            @RequestBody Map<String, Object> questionData,
            @AuthenticationPrincipal User currentUser) {

        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权创建题目");
        }

        try {
            Question question = questionService.createQuestion(questionData, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("id", question.getId());
            response.put("message", "题目创建成功");
            response.put("question", Map.of(
                "id", question.getId(),
                "type", question.getType(),
                "content", question.getContent(),
                "difficulty", question.getDifficulty(),
                "score", question.getScore()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("创建题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<Map<String, Object>> getQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal User currentUser) {

        try {
            Map<String, Object> questionDetail = questionService.getQuestionDetail(questionId, currentUser);
            return ResponseEntity.ok(questionDetail);

        } catch (Exception e) {
            throw new BusinessException("获取题目详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<Map<String, Object>> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> questionData,
            @AuthenticationPrincipal User currentUser) {

        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权修改题目");
        }

        try {
            Question question = questionService.updateQuestion(questionId, questionData, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "题目更新成功");
            response.put("question_id", questionId);
            response.put("updated_at", question.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("更新题目失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal User currentUser) {

        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权删除题目");
        }

        try {
            questionService.deleteQuestion(questionId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "题目删除成功");
            response.put("question_id", questionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("删除题目失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getQuestionStatistics(
            @AuthenticationPrincipal User currentUser) {

        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权访问统计数据");
        }

        try {
            Map<String, Object> statistics = questionService.getQuestionStatistics(currentUser);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            throw new BusinessException("获取题目统计失败: " + e.getMessage());
        }
    }
} 