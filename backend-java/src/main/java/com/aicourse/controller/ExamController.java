package com.aicourse.controller;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/exams")
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getExams(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> result = examService.getExams(skip, limit, courseId, status, currentUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createExam(@RequestBody Map<String, Object> examData) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = examService.createExam(examData, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<Map<String, Object>> getExam(@PathVariable Long examId) {
        User currentUser = getCurrentUser();
        Map<String, Object> exam = examService.getExam(examId, currentUser);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/{examId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateExam(
            @PathVariable Long examId,
            @RequestBody Map<String, Object> examData) {
        
        User currentUser = getCurrentUser();
        examService.updateExam(examId, examData, currentUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "考试更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{examId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteExam(@PathVariable Long examId) {
        User currentUser = getCurrentUser();
        examService.deleteExam(examId, currentUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "考试删除成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<Map<String, Object>> getExamQuestions(@PathVariable Long examId) {
        User currentUser = getCurrentUser();
        Map<String, Object> questions = examService.getExamQuestions(examId, currentUser);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{examId}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> publishExam(@PathVariable Long examId) {
        User currentUser = getCurrentUser();
        examService.publishExam(examId, currentUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "考试发布成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getExamStatistics() {
        User currentUser = getCurrentUser();
        Map<String, Object> statistics = examService.getExamStatistics(currentUser);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/{examId}/start")
    public ResponseEntity<Map<String, Object>> startExam(@PathVariable Long examId) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = examService.startExam(examId, currentUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{examId}/submit")
    public ResponseEntity<Map<String, Object>> submitExam(
            @PathVariable Long examId,
            @RequestBody Map<String, Object> answers) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> result = examService.submitExam(examId, answers, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{examId}/results")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getExamResults(@PathVariable Long examId) {
        User currentUser = getCurrentUser();
        Map<String, Object> results = examService.getExamResults(examId, currentUser);
        return ResponseEntity.ok(results);
    }

    /**
     * 获取当前用户 - 简化实现
     */
    private User getCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setRole(UserRole.ADMIN);
        return user;
    }
} 