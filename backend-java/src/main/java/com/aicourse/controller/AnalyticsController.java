package com.aicourse.controller;

import com.aicourse.entity.User;
import com.aicourse.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = analyticsService.getDashboardStats(currentUser);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user-dashboard/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboardStats(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = analyticsService.getUserDashboardStats(userId, currentUser);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-activities/{userId}")
    public ResponseEntity<Map<String, Object>> getRecentActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> activities = analyticsService.getRecentActivities(userId, limit, currentUser);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/learning-trends/{userId}")
    public ResponseEntity<Map<String, Object>> getLearningTrends(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> trends = analyticsService.getLearningTrends(userId, days, currentUser);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/knowledge-mastery/{userId}")
    public ResponseEntity<Map<String, Object>> getKnowledgeMastery(
            @PathVariable Long userId,
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> mastery = analyticsService.getKnowledgeMastery(userId, courseId, currentUser);
        return ResponseEntity.ok(mastery);
    }

    @GetMapping("/weak-points/{userId}")
    public ResponseEntity<Map<String, Object>> getWeakPoints(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> weakPoints = analyticsService.getWeakPoints(userId, limit, currentUser);
        return ResponseEntity.ok(weakPoints);
    }

    @GetMapping("/learning-recommendations/{userId}")
    public ResponseEntity<Map<String, Object>> getLearningRecommendations(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> recommendations = analyticsService.getLearningRecommendations(userId, currentUser);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/progress-stats/{userId}")
    public ResponseEntity<Map<String, Object>> getProgressStats(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> progressStats = analyticsService.getProgressStats(userId, currentUser);
        return ResponseEntity.ok(progressStats);
    }

    @GetMapping("/behavior-stats/{userId}")
    public ResponseEntity<Map<String, Object>> getBehaviorStats(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> behaviorStats = analyticsService.getBehaviorStats(userId, days, currentUser);
        return ResponseEntity.ok(behaviorStats);
    }

    @GetMapping("/learning-efficiency/{userId}")
    public ResponseEntity<Map<String, Object>> getLearningEfficiency(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> efficiency = analyticsService.getLearningEfficiency(userId, days, currentUser);
        return ResponseEntity.ok(efficiency);
    }

    @GetMapping("/grade-distribution/{userId}")
    public ResponseEntity<Map<String, Object>> getGradeDistribution(
            @PathVariable Long userId,
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> distribution = analyticsService.getGradeDistribution(userId, courseId, currentUser);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/subject-performance/{userId}")
    public ResponseEntity<Map<String, Object>> getSubjectPerformance(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> performance = analyticsService.getSubjectPerformance(userId, currentUser);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/grade-trend/{userId}")
    public ResponseEntity<Map<String, Object>> getGradeTrend(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> trend = analyticsService.getGradeTrend(userId, months, currentUser);
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/time-distribution/{userId}")
    public ResponseEntity<Map<String, Object>> getTimeDistribution(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> distribution = analyticsService.getTimeDistribution(userId, days, currentUser);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/activity-heatmap/{userId}")
    public ResponseEntity<Map<String, Object>> getActivityHeatmap(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "90") int days,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> heatmap = analyticsService.getActivityHeatmap(userId, days, currentUser);
        return ResponseEntity.ok(heatmap);
    }

    @GetMapping("/device-usage/{userId}")
    public ResponseEntity<Map<String, Object>> getDeviceUsage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> deviceUsage = analyticsService.getDeviceUsage(userId, days, currentUser);
        return ResponseEntity.ok(deviceUsage);
    }

    @GetMapping("/prediction/{userId}")
    public ResponseEntity<Map<String, Object>> getPredictionData(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> prediction = analyticsService.getPredictionData(userId, currentUser);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/course-analytics/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourseAnalytics(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> analytics = analyticsService.getCourseAnalytics(courseId, currentUser);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/class-performance/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getClassPerformance(
            @PathVariable Long classId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> performance = analyticsService.getClassPerformance(classId, currentUser);
        return ResponseEntity.ok(performance);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTeacherAnalytics(
            @PathVariable Long teacherId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> analytics = analyticsService.getTeacherAnalytics(teacherId, currentUser);
        return ResponseEntity.ok(analytics);
    }

    // 管理员专用API
    @GetMapping("/platform-usage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPlatformUsage(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> usage = analyticsService.getPlatformUsage(currentUser);
        return ResponseEntity.ok(usage);
    }

    @GetMapping("/system-metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemMetrics(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> metrics = analyticsService.getSystemMetrics(currentUser);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = analyticsService.getUserStatistics(currentUser);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourseStatistics(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = analyticsService.getCourseStatistics(currentUser);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/learning-progress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLearningProgress(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> progress = analyticsService.getLearningProgress(currentUser);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/task-completion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskCompletion(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> completion = analyticsService.getTaskCompletion(currentUser);
        return ResponseEntity.ok(completion);
    }

    @GetMapping("/exam-scores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getExamScores(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> scores = analyticsService.getExamScores(currentUser);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/knowledge-mastery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getKnowledgePointMastery(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> mastery = analyticsService.getKnowledgePointMastery(currentUser);
        return ResponseEntity.ok(mastery);
    }

    @GetMapping("/learning-path")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLearningPathAnalysis(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> analysis = analyticsService.getLearningPathAnalysis(currentUser);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/course-popularity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCoursePopularity(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> popularity = analyticsService.getCoursePopularity(currentUser);
        return ResponseEntity.ok(popularity);
    }
} 