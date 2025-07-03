package com.aicourse.controller;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.service.LearningPathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/learning-paths")
@CrossOrigin(origins = "*")
public class LearningPathController {

    @Autowired
    private LearningPathService learningPathService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLearningPaths(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningPathService.getLearningPaths(courseId, studentId, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/knowledge-point/{knowledgePointId}")
    public ResponseEntity<Map<String, Object>> getKnowledgePointLearningPath(
            @PathVariable Long knowledgePointId,
            @RequestParam(required = false) Long studentId) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningPathService.getKnowledgePointLearningPath(knowledgePointId, studentId, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recommended")
    public ResponseEntity<Map<String, Object>> getRecommendedLearningPath(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningPathService.getRecommendedLearningPath(courseId, studentId, currentUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{pathId}/start")
    public ResponseEntity<Map<String, Object>> startLearningPath(@PathVariable Long pathId) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningPathService.startLearningPath(pathId, currentUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{pathId}/nodes/{nodeId}/complete")
    public ResponseEntity<Map<String, Object>> completeLearningPathNode(
            @PathVariable Long pathId,
            @PathVariable Long nodeId) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = learningPathService.completeLearningPathNode(pathId, nodeId, currentUser);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Map<String, Object>>> generateLearningPath(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal User currentUser) {

        try {
            @SuppressWarnings("unchecked")
            List<Integer> targetKnowledgePoints = (List<Integer>) request.get("target_knowledge_points");
            Integer maxTimeDays = (Integer) request.getOrDefault("max_time_days", 30);
            @SuppressWarnings("unchecked")
            List<String> learningGoals = (List<String>) request.get("learning_goals");

            List<Map<String, Object>> recommendations = learningPathService.generatePersonalizedPath(
                    currentUser.getId(), targetKnowledgePoints, maxTimeDays, learningGoals);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            throw new BusinessException("生成学习路径失败: " + e.getMessage());
        }
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRecommendations(
            @PathVariable Long userId,
            @RequestParam List<Integer> knowledgePointIds,
            @AuthenticationPrincipal User currentUser) {

        // 权限检查
        if (!currentUser.getId().equals(userId) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        try {
            List<Map<String, Object>> recommendations = learningPathService.generatePersonalizedPath(
                    userId, knowledgePointIds, 30, null);

            Map<String, Object> response = new HashMap<>();
            response.put("user_id", userId);
            response.put("recommendations", recommendations.stream().map(rec -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("path_id", rec.get("path_id"));
                summary.put("path_name", rec.get("path_name"));
                summary.put("description", rec.get("description"));
                summary.put("total_time", rec.get("total_time"));
                summary.put("node_count", rec.get("node_count"));
                summary.put("reason", rec.get("reason"));
                summary.put("match_score", rec.get("match_score"));
                return summary;
            }).toArray());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("获取推荐失败: " + e.getMessage());
        }
    }

    @GetMapping("/path/{pathId}")
    public ResponseEntity<Map<String, Object>> getLearningPathDetail(
            @PathVariable String pathId,
            @AuthenticationPrincipal User currentUser) {

        try {
            Map<String, Object> pathDetail = learningPathService.getPathDetail(pathId, currentUser);
            return ResponseEntity.ok(pathDetail);

        } catch (Exception e) {
            throw new BusinessException("获取学习路径详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/progress/update")
    public ResponseEntity<Map<String, Object>> updateLearningProgress(
            @RequestBody Map<String, Object> progressData,
            @AuthenticationPrincipal User currentUser) {

        try {
            String pathId = (String) progressData.get("path_id");
            Integer nodeId = (Integer) progressData.get("node_id");
            Double progress = Double.valueOf(progressData.get("progress").toString());

            learningPathService.updateProgress(pathId, nodeId, progress, currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "学习进度更新成功");
            response.put("path_id", pathId);
            response.put("node_id", nodeId);
            response.put("progress", progress);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("更新学习进度失败: " + e.getMessage());
        }
    }

    @GetMapping("/analytics/{userId}")
    public ResponseEntity<Map<String, Object>> getLearningPathAnalytics(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {

        // 权限检查
        if (!currentUser.getId().equals(userId) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        try {
            Map<String, Object> analytics = learningPathService.getAnalytics(userId);
            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            throw new BusinessException("获取学习路径分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/schedule/{userId}")
    public ResponseEntity<Map<String, Object>> getStudyScheduleRecommendation(
            @PathVariable Long userId,
            @RequestParam(required = false) String pathId,
            @RequestParam(required = false) String deadline,
            @AuthenticationPrincipal User currentUser) {

        // 权限检查
        if (!currentUser.getId().equals(userId) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        try {
            Map<String, Object> schedule = learningPathService.getStudyScheduleRecommendation(
                    userId, pathId, deadline);

            return ResponseEntity.ok(schedule);

        } catch (Exception e) {
            throw new BusinessException("获取学习时间安排失败: " + e.getMessage());
        }
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