package com.aicourse.controller;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.service.LearningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/learning-records")
public class LearningRecordController {

    @Autowired
    private LearningRecordService learningRecordService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createLearningRecord(@RequestBody Map<String, Object> recordData) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningRecordService.createLearningRecord(recordData, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentLearningRecords(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) String actionType,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningRecordService.getStudentLearningRecords(
            studentId, knowledgePointId, actionType, skip, limit, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/knowledge-point/{knowledgePointId}/statistics")
    public ResponseEntity<Map<String, Object>> getKnowledgePointLearningStatistics(@PathVariable Long knowledgePointId) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningRecordService.getKnowledgePointLearningStatistics(knowledgePointId, currentUser);
        return ResponseEntity.ok(result);
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