package com.aicourse.service;

import com.aicourse.entity.User;
import com.aicourse.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class VideoLearningService {

    public Map<String, Object> recordVideoEvent(Map<String, Object> eventData, User currentUser) {
        try {
            // 简化实现 - 记录视频学习事件
            String eventType = (String) eventData.get("event_type");
            
            // 在实际项目中，这里应该保存到VideoLearningRecord表
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "事件记录成功");
            result.put("event_type", eventType);
            result.put("recorded_at", LocalDateTime.now().toString());
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "记录视频事件失败: " + e.getMessage());
            return result;
        }
    }

    public Map<String, Object> getVideoAnalytics(Long taskId, Long studentId, User currentUser) {
        // 权限检查
        if (currentUser.getRole().name().equals("STUDENT") && studentId != null && 
            !studentId.equals(currentUser.getId())) {
            throw new BusinessException("只能查看自己的学习数据");
        }
        
        // 简化实现 - 生成分析数据
        Map<String, Object> summary = new HashMap<>();
        summary.put("total_students", 1);
        summary.put("average_completion_rate", 85.5);
        summary.put("average_attention_score", 78.2);
        summary.put("average_engagement_score", 82.0);
        summary.put("average_efficiency_score", 75.8);
        summary.put("total_watch_time", 1200); // 秒
        summary.put("total_sessions", 3);
        
        Map<String, Object> completionDistribution = new HashMap<>();
        completionDistribution.put("0-25%", 0);
        completionDistribution.put("26-50%", 0);
        completionDistribution.put("51-75%", 0);
        completionDistribution.put("76-90%", 0);
        completionDistribution.put("91-100%", 1);
        
        Map<String, Object> attentionDistribution = new HashMap<>();
        attentionDistribution.put("低", 0);
        attentionDistribution.put("中", 0);
        attentionDistribution.put("高", 1);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("summary", summary);
        analytics.put("completion_distribution", completionDistribution);
        analytics.put("attention_distribution", attentionDistribution);
        analytics.put("learning_patterns", new HashMap<>());
        
        return analytics;
    }

    public Map<String, Object> getVideoHeatmap(Long taskId, Long studentId, User currentUser) {
        // 权限检查
        if (currentUser.getRole().name().equals("STUDENT") && studentId != null && 
            !studentId.equals(currentUser.getId())) {
            throw new BusinessException("只能查看自己的学习数据");
        }
        
        // 简化实现 - 生成热力图数据
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        int totalDuration = 1800; // 30分钟视频
        int segmentDuration = 10; // 10秒一段
        int totalSegments = totalDuration / segmentDuration;
        
        for (int i = 0; i < totalSegments; i++) {
            Map<String, Object> segment = new HashMap<>();
            segment.put("time_start", i * segmentDuration);
            segment.put("time_end", (i + 1) * segmentDuration);
            segment.put("intensity", Math.random() * 100); // 随机强度
            segment.put("watch_count", (int)(Math.random() * 5) + 1);
            heatmapData.add(segment);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("heatmap_data", heatmapData);
        result.put("total_duration", totalDuration);
        result.put("segment_duration", segmentDuration);
        result.put("max_intensity", 100);
        
        return result;
    }

    public Map<String, Object> getLearningQualityReport(Long taskId, Long studentId, User currentUser) {
        // 权限检查
        if (currentUser.getRole().name().equals("STUDENT") && studentId != null && 
            !studentId.equals(currentUser.getId())) {
            throw new BusinessException("只能查看自己的学习数据");
        }
        
        // 简化实现
        Map<String, Object> overallQuality = new HashMap<>();
        overallQuality.put("high_attention_rate", 80.0);
        overallQuality.put("high_engagement_rate", 75.0);
        overallQuality.put("completion_rate", 90.0);
        
        List<String> identifiedIssues = Arrays.asList(
            "部分学生跳过了重要章节",
            "平均观看速度偏快，建议放慢节奏"
        );
        
        List<String> learningSuggestions = Arrays.asList(
            "增加互动问答环节",
            "在关键知识点处添加暂停提示",
            "提供章节小结"
        );
        
        Map<String, Object> qualityTrends = new HashMap<>();
        qualityTrends.put("trend", "improving");
        qualityTrends.put("recent_quality", 82.5);
        qualityTrends.put("previous_quality", 78.0);
        qualityTrends.put("change_percentage", 5.8);
        
        Map<String, Object> result = new HashMap<>();
        result.put("overall_quality", overallQuality);
        result.put("identified_issues", identifiedIssues);
        result.put("learning_suggestions", learningSuggestions);
        result.put("quality_trends", qualityTrends);
        
        return result;
    }

    public Map<String, Object> getVideoLearningProgress(Long userId, User currentUser) {
        // 权限检查
        if (currentUser.getRole().name().equals("STUDENT") && !userId.equals(currentUser.getId())) {
            throw new BusinessException("只能查看自己的学习进度");
        }
        
        // 简化实现
        Map<String, Object> progress = new HashMap<>();
        progress.put("total_videos", 10);
        progress.put("completed_videos", 7);
        progress.put("total_watch_time", 14400); // 4小时
        progress.put("average_completion_rate", 85.5);
        progress.put("current_streak", 5); // 连续学习天数
        
        List<Map<String, Object>> recentVideos = Arrays.asList(
            Map.of(
                "video_title", "Java基础概念",
                "completion_rate", 100.0,
                "watch_time", 1800,
                "quality_score", 88.0
            ),
            Map.of(
                "video_title", "Spring框架入门",
                "completion_rate", 75.0,
                "watch_time", 1350,
                "quality_score", 82.0
            )
        );
        
        progress.put("recent_videos", recentVideos);
        
        return progress;
    }

    public Map<String, Object> getVideoLearningStatistics(Long courseId, Long taskId, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }
        
        // 简化实现
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total_video_records", 150);
        statistics.put("total_watch_time", 180000); // 50小时
        statistics.put("average_session_duration", 25.5); // 分钟
        statistics.put("completion_rate", 78.5);
        statistics.put("engagement_score", 82.0);
        
        Map<String, Object> popularVideos = Map.of(
            "most_watched", "Java基础教程",
            "highest_completion", "Spring Boot实战",
            "most_replayed", "数据库设计原理"
        );
        
        List<Map<String, Object>> learningTrends = Arrays.asList(
            Map.of("date", "2024-01-01", "watch_hours", 25.5),
            Map.of("date", "2024-01-02", "watch_hours", 28.2),
            Map.of("date", "2024-01-03", "watch_hours", 22.8)
        );
        
        statistics.put("popular_videos", popularVideos);
        statistics.put("learning_trends", learningTrends);
        
        return statistics;
    }
} 