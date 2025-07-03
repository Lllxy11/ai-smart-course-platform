package com.aicourse.service;

import com.aicourse.entity.Notification;
import com.aicourse.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知提醒服务
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    private final ObjectMapper objectMapper;

    // 通知频率限制缓存（用户ID -> 模板ID -> 最后发送时间）
    private final Map<Long, Map<String, LocalDateTime>> rateLimitCache = new ConcurrentHashMap<>();

    // 预定义通知模板
    private final Map<String, NotificationTemplate> templates;

    // 通知频率限制（秒）
    private final Map<String, Integer> rateLimits;

    public NotificationService() {
        this.objectMapper = new ObjectMapper();
        this.templates = initializeTemplates();
        this.rateLimits = initializeRateLimits();
    }

    /**
     * 通知模板数据类
     */
    public static class NotificationTemplate {
        private final String templateId;
        private final String title;
        private final String content;
        private final String category;
        private final String priority;
        private final boolean autoDismiss;
        private final int dismissAfter;

        public NotificationTemplate(String templateId, String title, String content, 
                                   String category, String priority, boolean autoDismiss, int dismissAfter) {
            this.templateId = templateId;
            this.title = title;
            this.content = content;
            this.category = category;
            this.priority = priority;
            this.autoDismiss = autoDismiss;
            this.dismissAfter = dismissAfter;
        }

        // Getters
        public String getTemplateId() { return templateId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getCategory() { return category; }
        public String getPriority() { return priority; }
        public boolean isAutoDismiss() { return autoDismiss; }
        public int getDismissAfter() { return dismissAfter; }
    }

    /**
     * 通知数据类
     */
    public static class NotificationData {
        private String id;
        private Long userId;
        private String title;
        private String content;
        private String category;
        private String priority;
        private boolean isRead;
        private LocalDateTime createdAt;
        private Map<String, Object> data;
        private String actionUrl;

        // 构造函数和Getters/Setters
        public NotificationData() {}

        public NotificationData(String id, Long userId, String title, String content, 
                               String category, String priority, boolean isRead, 
                               LocalDateTime createdAt, Map<String, Object> data, String actionUrl) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.content = content;
            this.category = category;
            this.priority = priority;
            this.isRead = isRead;
            this.createdAt = createdAt;
            this.data = data;
            this.actionUrl = actionUrl;
        }

        // Getters和Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    }

    /**
     * 发送通知
     */
    public NotificationData sendNotification(
            Long userId,
            String templateId,
            Map<String, Object> customData,
            String actionUrl) {

        // 检查模板是否存在
        if (!templates.containsKey(templateId)) {
            logger.warn("通知模板不存在: {}", templateId);
            return null;
        }

        NotificationTemplate template = templates.get(templateId);

        // 检查频率限制
        if (!checkRateLimit(userId, templateId)) {
            logger.info("通知频率限制: userId={}, templateId={}", userId, templateId);
            return null;
        }

        // 创建通知数据
        NotificationData notificationData = new NotificationData(
                generateNotificationId(userId, templateId),
                userId,
                template.getTitle(),
                formatContent(template.getContent(), customData),
                template.getCategory(),
                template.getPriority(),
                false,
                LocalDateTime.now(),
                customData,
                actionUrl
        );

        // 保存到数据库
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(notificationData.getTitle());
            notification.setContent(notificationData.getContent());
            notification.setCategory(notificationData.getCategory());
            notification.setPriority(notificationData.getPriority());
            notification.setData(customData != null ? objectMapper.writeValueAsString(customData) : null);
            notification.setActionUrl(actionUrl);
            notification.setIsRead(false);
            notification.setCreatedAt(notificationData.getCreatedAt());

            notification = notificationRepository.save(notification);
            notificationData.setId(String.valueOf(notification.getId()));

        } catch (Exception e) {
            logger.error("保存通知失败: {}", e.getMessage());
            return null;
        }

        // 记录到频率限制缓存
        recordNotification(userId, templateId);

        logger.info("通知已发送: userId={}, templateId={}", userId, templateId);
        return notificationData;
    }

    /**
     * 获取用户通知列表
     */
    public List<NotificationData> getUserNotifications(
            Long userId, 
            int limit, 
            boolean unreadOnly, 
            String category) {

        List<Notification> notifications;

        if (category != null && !category.isEmpty()) {
            if (unreadOnly) {
                notifications = notificationRepository.findByUserIdAndCategoryAndIsReadOrderByCreatedAtDesc(
                    userId, category, false, PageRequest.of(0, limit));
            } else {
                notifications = notificationRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(
                    userId, category, PageRequest.of(0, limit));
            }
        } else {
            if (unreadOnly) {
                notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
                    userId, false, PageRequest.of(0, limit));
            } else {
                notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, PageRequest.of(0, limit));
            }
        }

        List<NotificationData> result = new ArrayList<>();
        for (Notification notif : notifications) {
            Map<String, Object> data = null;
            if (notif.getData() != null) {
                try {
                    data = objectMapper.readValue(notif.getData(), new TypeReference<Map<String, Object>>() {});
                } catch (JsonProcessingException e) {
                    logger.warn("解析通知数据失败: {}", e.getMessage());
                }
            }

            result.add(new NotificationData(
                    String.valueOf(notif.getId()),
                    notif.getUserId(),
                    notif.getTitle(),
                    notif.getContent(),
                    notif.getCategory(),
                    notif.getPriority(),
                    notif.getIsRead(),
                    notif.getCreatedAt(),
                    data,
                    notif.getActionUrl()
            ));
        }

        return result;
    }

    /**
     * 标记通知为已读
     */
    public boolean markAsRead(Long notificationId, Long userId) {
        try {
            Optional<Notification> optionalNotification = notificationRepository
                    .findByIdAndUserId(notificationId, userId);

            if (optionalNotification.isPresent()) {
                Notification notification = optionalNotification.get();
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
                return true;
            }

        } catch (Exception e) {
            logger.error("标记通知已读失败: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 标记所有通知为已读
     */
    public int markAllAsRead(Long userId, String category) {
        try {
            int count;
            if (category != null && !category.isEmpty()) {
                count = notificationRepository.markAllAsReadByUserIdAndCategory(userId, category, LocalDateTime.now());
            } else {
                count = notificationRepository.markAllAsReadByUserId(userId, LocalDateTime.now());
            }
            return count;

        } catch (Exception e) {
            logger.error("批量标记已读失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 删除通知
     */
    public boolean deleteNotification(Long notificationId, Long userId) {
        try {
            int deletedCount = notificationRepository.deleteByIdAndUserId(notificationId, userId);
            return deletedCount > 0;

        } catch (Exception e) {
            logger.error("删除通知失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取通知摘要
     */
    public Map<String, Object> getNotificationSummary(Long userId) {
        // 统计各类未读通知数量
        Map<String, Long> unreadCounts = new HashMap<>();
        String[] categories = {"system", "learning", "social", "achievement"};

        for (String category : categories) {
            long count = notificationRepository.countByUserIdAndCategoryAndIsRead(userId, category, false);
            unreadCounts.put(category, count);
        }

        // 总未读数
        long totalUnread = unreadCounts.values().stream().mapToLong(Long::longValue).sum();

        // 最近通知
        List<NotificationData> recentNotifications = getUserNotifications(userId, 3, true, null);

        // 检查是否有紧急通知
        boolean hasUrgent = recentNotifications.stream()
                .anyMatch(notif -> "urgent".equals(notif.getPriority()));

        Map<String, Object> result = new HashMap<>();
        result.put("total_unread", totalUnread);
        result.put("unread_by_category", unreadCounts);
        result.put("recent_notifications", recentNotifications);
        result.put("has_urgent", hasUrgent);

        return result;
    }

    /**
     * 批量发送通知
     */
    public int sendBatchNotification(
            List<Long> userIds,
            String templateId,
            Map<String, Object> customData) {

        int successCount = 0;

        for (Long userId : userIds) {
            if (sendNotification(userId, templateId, customData, null) != null) {
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * 创建学习提醒
     */
    public void createLearningReminders(Long userId) {
        // 检查用户今天是否已经学习
        // 这里可以查询学习记录，判断是否需要提醒
        // 简化实现，直接发送提醒

        Map<String, Object> customData = Map.of("time", "今天");
        sendNotification(userId, "idle_reminder", customData, null);
    }

    /**
     * 创建成就通知
     */
    public void createAchievementNotification(
            Long userId,
            String achievementType,
            Map<String, Object> achievementData) {

        if ("level_up".equals(achievementType)) {
            sendNotification(userId, "level_up", achievementData, "/profile");
        } else if ("streak".equals(achievementType)) {
            sendNotification(userId, "streak_milestone", achievementData, null);
        }
    }

    /**
     * 创建截止日期提醒（定时任务调用）
     */
    public void createDeadlineReminders() {
        // 查找即将截止的任务
        // 这里应该查询即将截止的任务和作业
        // 简化实现
        logger.info("定时创建截止日期提醒");
    }

    // 私有辅助方法

    private String generateNotificationId(Long userId, String templateId) {
        return userId + "_" + templateId + "_" + System.currentTimeMillis();
    }

    private boolean checkRateLimit(Long userId, String templateId) {
        int limitSeconds = rateLimits.getOrDefault(templateId, rateLimits.get("default"));
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(limitSeconds);

        Map<String, LocalDateTime> userLimits = rateLimitCache.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        LocalDateTime lastSent = userLimits.get(templateId);
        if (lastSent != null && lastSent.isAfter(cutoffTime)) {
            return false;
        }

        return true;
    }

    private void recordNotification(Long userId, String templateId) {
        Map<String, LocalDateTime> userLimits = rateLimitCache.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        userLimits.put(templateId, LocalDateTime.now());

        // 限制缓存大小
        if (userLimits.size() > 50) {
            userLimits.clear(); // 简化实现，实际可以删除最旧的记录
        }
    }

    private String formatContent(String content, Map<String, Object> customData) {
        if (customData == null || customData.isEmpty()) {
            return content;
        }

        try {
            String result = content;
            for (Map.Entry<String, Object> entry : customData.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                if (result.contains(placeholder)) {
                    result = result.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }
            return result;
        } catch (Exception e) {
            logger.warn("通知内容格式化失败: {}", e.getMessage());
            return content;
        }
    }

    private Map<String, NotificationTemplate> initializeTemplates() {
        Map<String, NotificationTemplate> templates = new HashMap<>();

        // 学习相关通知
        templates.put("task_deadline", new NotificationTemplate(
                "task_deadline", "任务截止提醒", "您有任务即将截止，请及时完成",
                "learning", "high", false, 5000));

        templates.put("course_updated", new NotificationTemplate(
                "course_updated", "课程更新", "您关注的课程有新的内容更新",
                "learning", "normal", true, 5000));

        templates.put("assignment_graded", new NotificationTemplate(
                "assignment_graded", "作业已评分", "您的作业已经完成评分，点击查看详情",
                "learning", "normal", true, 5000));

        // 系统通知
        templates.put("system_maintenance", new NotificationTemplate(
                "system_maintenance", "系统维护通知", "系统将进行维护，请提前保存您的工作",
                "system", "high", false, 5000));

        templates.put("account_security", new NotificationTemplate(
                "account_security", "账户安全提醒", "检测到异常登录，请确认账户安全",
                "system", "urgent", false, 5000));

        // 成就通知
        templates.put("level_up", new NotificationTemplate(
                "level_up", "等级提升", "恭喜您的学习等级提升了！",
                "achievement", "normal", true, 5000));

        templates.put("streak_milestone", new NotificationTemplate(
                "streak_milestone", "连续学习里程碑", "您已连续学习多天，坚持得很棒！",
                "achievement", "normal", true, 5000));

        // 学习监控通知
        templates.put("idle_reminder", new NotificationTemplate(
                "idle_reminder", "学习提醒", "您已经休息了一段时间，要不要继续学习？",
                "learning", "low", true, 5000));

        templates.put("focus_break", new NotificationTemplate(
                "focus_break", "休息提醒", "您已经专注学习很久了，建议休息一下",
                "learning", "normal", true, 5000));

        return templates;
    }

    private Map<String, Integer> initializeRateLimits() {
        Map<String, Integer> limits = new HashMap<>();
        limits.put("idle_reminder", 1800);  // 30分钟内最多一次
        limits.put("focus_break", 3600);    // 1小时内最多一次
        limits.put("default", 300);         // 5分钟内最多一次
        return limits;
    }


} 