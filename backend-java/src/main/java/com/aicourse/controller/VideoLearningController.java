package com.aicourse.controller;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.service.VideoLearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/video-learning")
public class VideoLearningController {

    @Autowired
    private VideoLearningService videoLearningService;

    @PostMapping("/record-event")
    public ResponseEntity<Map<String, Object>> recordVideoEvent(@RequestBody Map<String, Object> eventData) {
        User currentUser = getCurrentUser();
        Map<String, Object> result = videoLearningService.recordVideoEvent(eventData, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/{taskId}")
    public ResponseEntity<Map<String, Object>> getVideoAnalytics(
            @PathVariable Long taskId,
            @RequestParam(required = false) Long studentId) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> analytics = videoLearningService.getVideoAnalytics(taskId, studentId, currentUser);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/heatmap/{taskId}")
    public ResponseEntity<Map<String, Object>> getVideoHeatmap(
            @PathVariable Long taskId,
            @RequestParam(required = false) Long studentId) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> heatmap = videoLearningService.getVideoHeatmap(taskId, studentId, currentUser);
        return ResponseEntity.ok(heatmap);
    }

    @GetMapping("/quality-report/{taskId}")
    public ResponseEntity<Map<String, Object>> getLearningQualityReport(
            @PathVariable Long taskId,
            @RequestParam(required = false) Long studentId) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> qualityReport = videoLearningService.getLearningQualityReport(taskId, studentId, currentUser);
        return ResponseEntity.ok(qualityReport);
    }

    @GetMapping("/progress/{userId}")
    public ResponseEntity<Map<String, Object>> getVideoLearningProgress(@PathVariable Long userId) {
        User currentUser = getCurrentUser();
        Map<String, Object> progress = videoLearningService.getVideoLearningProgress(userId, currentUser);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getVideoLearningStatistics(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long taskId) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> statistics = videoLearningService.getVideoLearningStatistics(courseId, taskId, currentUser);
        return ResponseEntity.ok(statistics);
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