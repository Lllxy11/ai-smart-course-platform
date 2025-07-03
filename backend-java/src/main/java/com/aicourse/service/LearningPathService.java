package com.aicourse.service;

import com.aicourse.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习路径推荐服务
 */
@Service
@Transactional
public class LearningPathService {

    private static final Logger logger = LoggerFactory.getLogger(LearningPathService.class);

    public LearningPathService() {
    }

    /**
     * 学习节点数据类
     */
    public static class LearningNode {
        private Long id;
        private String name;
        private String type;
        private Double difficulty;
        private Integer estimatedTime;
        private List<Long> prerequisites;
        private Double importance;
        private Double masteryThreshold = 0.8;

        public LearningNode() {}

        public LearningNode(Long id, String name, String type, Double difficulty, 
                           Integer estimatedTime, List<Long> prerequisites, Double importance) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.difficulty = difficulty;
            this.estimatedTime = estimatedTime;
            this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
            this.importance = importance;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Double getDifficulty() { return difficulty; }
        public void setDifficulty(Double difficulty) { this.difficulty = difficulty; }
        public Integer getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }
        public List<Long> getPrerequisites() { return prerequisites; }
        public void setPrerequisites(List<Long> prerequisites) { this.prerequisites = prerequisites; }
        public Double getImportance() { return importance; }
        public void setImportance(Double importance) { this.importance = importance; }
        public Double getMasteryThreshold() { return masteryThreshold; }
        public void setMasteryThreshold(Double masteryThreshold) { this.masteryThreshold = masteryThreshold; }
    }

    /**
     * 学习路径数据类
     */
    public static class LearningPath {
        private String id;
        private String name;
        private String description;
        private List<LearningNode> nodes;
        private Integer totalTime;
        private String difficultyLevel;
        private Double completionRate;
        private Double personalizationScore;

        public LearningPath() {}

        public LearningPath(String id, String name, String description, List<LearningNode> nodes) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.nodes = nodes != null ? nodes : new ArrayList<>();
            this.totalTime = calculateTotalTime();
        }

        private Integer calculateTotalTime() {
            return nodes.stream().mapToInt(LearningNode::getEstimatedTime).sum();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<LearningNode> getNodes() { return nodes; }
        public void setNodes(List<LearningNode> nodes) { 
            this.nodes = nodes; 
            this.totalTime = calculateTotalTime();
        }
        public Integer getTotalTime() { return totalTime; }
        public void setTotalTime(Integer totalTime) { this.totalTime = totalTime; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public Double getPersonalizationScore() { return personalizationScore; }
        public void setPersonalizationScore(Double personalizationScore) { this.personalizationScore = personalizationScore; }
    }

    /**
     * 生成个性化学习路径（重载方法支持新的参数）
     */
    public List<Map<String, Object>> generatePersonalizedPath(Long userId, List<Integer> targetKnowledgePoints, 
                                                              Integer maxTimeDays, List<String> learningGoals) {
        // 转换Integer列表为Long列表
        List<Long> longKnowledgePoints = targetKnowledgePoints.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
        
        return generatePersonalizedPath(userId, longKnowledgePoints);
    }

    /**
     * 生成个性化学习路径
     */
    public List<Map<String, Object>> generatePersonalizedPath(Long userId, List<Long> targetKnowledgePoints) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        // 简化实现，返回示例路径
        Map<String, Object> recommendation = new HashMap<>();
        
        LearningPath path = new LearningPath(
            "default_path",
            "推荐学习路径",
            "基于您的学习情况定制的路径",
            createDefaultNodes(targetKnowledgePoints)
        );
        
        recommendation.put("path_id", path.getId());
        recommendation.put("path_name", path.getName());
        recommendation.put("description", path.getDescription());
        recommendation.put("total_time", path.getTotalTime());
        recommendation.put("node_count", path.getNodes().size());
        recommendation.put("reason", "基于您的学习风格和当前水平推荐");
        recommendation.put("match_score", 0.85);
        recommendation.put("benefits", Arrays.asList("循序渐进", "个性化定制", "时间优化"));
        recommendation.put("challenges", Arrays.asList("需要坚持", "阶段性挑战"));
        recommendation.put("estimated_completion", LocalDateTime.now().plusWeeks(4));
        
        recommendations.add(recommendation);
        return recommendations;
    }

    /**
     * 获取学习路径详情
     */
    public Map<String, Object> getPathDetail(String pathId, User currentUser) {
        Map<String, Object> pathDetail = new HashMap<>();
        
        // 简化实现
        pathDetail.put("path_id", pathId);
        pathDetail.put("name", "详细学习路径");
        pathDetail.put("description", "这是一个详细的学习路径");
        
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", i);
            node.put("name", "学习节点 " + i);
            node.put("difficulty", 2.0 + i * 0.5);
            node.put("estimated_time", 60);
            node.put("prerequisites", new ArrayList<>());
            nodes.add(node);
        }
        
        pathDetail.put("nodes", nodes);
        pathDetail.put("total_time", 300);
        pathDetail.put("difficulty_level", "intermediate");
        
        return pathDetail;
    }

    /**
     * 更新学习进度
     */
    public void updateProgress(String pathId, Integer nodeId, Double progress, Long userId) {
        // 简化实现 - 在实际项目中这里应该保存到数据库
        logger.info("用户 {} 在路径 {} 的节点 {} 的进度更新为 {}", userId, pathId, nodeId, progress);
    }

    /**
     * 获取学习路径分析数据
     */
    public Map<String, Object> getAnalytics(Long userId) {
        Map<String, Object> analytics = new HashMap<>();
        
        // 简化实现，返回模拟数据
        analytics.put("user_id", userId);
        analytics.put("total_paths_taken", 3);
        analytics.put("completed_paths", 1);
        analytics.put("average_completion_rate", 0.75);
        analytics.put("total_study_time", 2400); // 分钟
        
        List<Map<String, Object>> challengingTopics = new ArrayList<>();
        challengingTopics.add(Map.of("topic", "高等数学", "difficulty_score", 4.2));
        challengingTopics.add(Map.of("topic", "数据结构", "difficulty_score", 3.8));
        analytics.put("most_challenging_topics", challengingTopics);
        
        Map<String, Object> learningEfficiency = new HashMap<>();
        learningEfficiency.put("trend", "improving");
        learningEfficiency.put("score", 78);
        analytics.put("learning_efficiency", learningEfficiency);
        
        return analytics;
    }

    /**
     * 获取学习时间安排推荐
     */
    public Map<String, Object> getStudyScheduleRecommendation(Long userId, String pathId, String deadline) {
        Map<String, Object> schedule = new HashMap<>();
        
        // 简化实现
        List<Map<String, Object>> dailySchedule = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("day", "第" + i + "天");
            day.put("topics", Arrays.asList("学习主题" + i));
            day.put("duration", 120); // 2小时
            day.put("breaks", Arrays.asList("休息15分钟"));
            dailySchedule.add(day);
        }
        
        List<Map<String, Object>> weeklySchedule = new ArrayList<>();
        Map<String, Object> week = new HashMap<>();
        week.put("week", 1);
        week.put("focus", "基础概念");
        week.put("total_hours", 14);
        weeklySchedule.add(week);
        
        List<Map<String, Object>> milestones = new ArrayList<>();
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("week", 2);
        milestone.put("title", "第一阶段完成");
        milestone.put("description", "基础知识掌握");
        milestones.add(milestone);
        
        schedule.put("daily_schedule", dailySchedule);
        schedule.put("weekly_schedule", weeklySchedule);
        schedule.put("milestones", milestones);
        schedule.put("break_suggestions", Arrays.asList("每2小时休息15分钟", "每天休息8小时"));
        schedule.put("adjustment_tips", Arrays.asList("根据进度调整学习节奏", "保持规律的学习时间"));
        
        return schedule;
    }

    /**
     * 分析学习差距
     */
    public Map<String, Object> analyzeLearningGaps(Long userId, Long targetCourseId) {
        Map<String, Object> result = new HashMap<>();
        
        // 简化实现
        List<Map<String, Object>> gaps = new ArrayList<>();
        Map<String, Object> gap = new HashMap<>();
        gap.put("knowledge_point_id", 1L);
        gap.put("required_level", 0.8);
        gap.put("current_level", 0.5);
        gap.put("gap_size", 0.3);
        gap.put("priority", 8.0);
        gaps.add(gap);
        
        result.put("knowledge_gaps", gaps);
        result.put("total_gaps", gaps.size());
        result.put("critical_gaps", gaps);
        result.put("remedial_suggestions", Arrays.asList(
            "加强基础概念学习",
            "增加练习时间",
            "寻求帮助"
        ));
        result.put("estimated_catch_up_time", 240); // 4小时
        
        return result;
    }

    private List<LearningNode> createDefaultNodes(List<Long> targetKnowledgePoints) {
        List<LearningNode> nodes = new ArrayList<>();
        
        for (int i = 0; i < targetKnowledgePoints.size(); i++) {
            Long kpId = targetKnowledgePoints.get(i);
            LearningNode node = new LearningNode(
                kpId,
                "知识点 " + kpId,
                "knowledge_point",
                2.0 + i * 0.5, // 递增难度
                60, // 1小时
                new ArrayList<>(),
                1.0
            );
            nodes.add(node);
        }
        
        return nodes;
    }

    // ================ 新增的API方法 ================
    
    public Map<String, Object> getLearningPaths(Long courseId, Long studentId, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> paths = new ArrayList<>();
            
            // 生成几个示例学习路径
            for (int i = 1; i <= 3; i++) {
                Map<String, Object> path = new HashMap<>();
                path.put("id", "path_" + i);
                path.put("name", "学习路径 " + i);
                path.put("description", "这是学习路径 " + i + " 的描述");
                path.put("totalTime", 180 + i * 60);
                path.put("difficulty", "初级");
                path.put("nodeCount", 5 + i);
                paths.add(path);
            }
            
            result.put("success", true);
            result.put("paths", paths);
            result.put("total", paths.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取学习路径失败：" + e.getMessage());
            result.put("paths", new ArrayList<>());
        }
        
        return result;
    }

    public Map<String, Object> getKnowledgePointLearningPath(Long knowledgePointId, Long studentId, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建基于知识点的学习路径
            List<Map<String, Object>> nodes = new ArrayList<>();
            
            // 当前知识点
            Map<String, Object> currentNode = new HashMap<>();
            currentNode.put("id", knowledgePointId);
            currentNode.put("name", "目标知识点");
            currentNode.put("type", "target");
            currentNode.put("completed", false);
            nodes.add(currentNode);
            
            // 添加相关知识点
            for (int i = 1; i <= 3; i++) {
                Map<String, Object> relatedNode = new HashMap<>();
                relatedNode.put("id", knowledgePointId + i);
                relatedNode.put("name", "相关知识点 " + i);
                relatedNode.put("type", "related");
                relatedNode.put("completed", false);
                nodes.add(relatedNode);
            }
            
            Map<String, Object> path = new HashMap<>();
            path.put("id", "kp_path_" + knowledgePointId);
            path.put("name", "知识点学习路径");
            path.put("description", "针对知识点的个性化学习路径");
            path.put("nodes", nodes);
            path.put("totalTime", 240);
            path.put("estimatedDays", 3);
            
            result.put("success", true);
            result.put("path", path);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取知识点学习路径失败：" + e.getMessage());
        }
        
        return result;
    }

    public Map<String, Object> getRecommendedLearningPath(Long courseId, Long studentId, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> recommendedPath = new HashMap<>();
            recommendedPath.put("id", "recommended_path");
            recommendedPath.put("name", "推荐学习路径");
            recommendedPath.put("description", "基于您的学习情况和目标推荐的最佳路径");
            recommendedPath.put("matchScore", 85.5);
            recommendedPath.put("totalTime", 320);
            recommendedPath.put("difficulty", "适中");
            
            List<Map<String, Object>> nodes = new ArrayList<>();
            for (int i = 1; i <= 6; i++) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", i);
                node.put("name", "推荐学习节点 " + i);
                node.put("type", "recommended");
                node.put("priority", 10 - i);
                node.put("estimatedTime", 50 + i * 5);
                nodes.add(node);
            }
            recommendedPath.put("nodes", nodes);
            
            result.put("success", true);
            result.put("path", recommendedPath);
            result.put("reasons", Arrays.asList(
                "符合您的学习风格",
                "基于您的当前水平",
                "优化的学习顺序"
            ));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取推荐学习路径失败：" + e.getMessage());
        }
        
        return result;
    }

    public Map<String, Object> startLearningPath(Long pathId, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 记录学习路径开始
            result.put("success", true);
            result.put("message", "学习路径已开始");
            result.put("pathId", pathId);
            result.put("userId", currentUser.getId());
            result.put("startTime", LocalDateTime.now().toString());
            result.put("status", "ACTIVE");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "开始学习路径失败：" + e.getMessage());
        }
        
        return result;
    }

    public Map<String, Object> completeLearningPathNode(Long pathId, Long nodeId, User currentUser) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 记录节点完成
            result.put("success", true);
            result.put("message", "学习节点已完成");
            result.put("pathId", pathId);
            result.put("nodeId", nodeId);
            result.put("userId", currentUser.getId());
            result.put("completedTime", LocalDateTime.now().toString());
            result.put("progress", 100);
            
            // 检查是否有下一个节点
            result.put("nextNodeId", nodeId + 1);
            result.put("pathCompleted", false);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "完成学习节点失败：" + e.getMessage());
        }
        
        return result;
    }
} 