package com.aicourse.controller;


import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知管理控制器
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "通知管理", description = "通知相关API接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取用户通知列表", description = "获取当前用户的通知列表，支持筛选")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(
            @Parameter(description = "跳过数量") @RequestParam(defaultValue = "0") int skip,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "只显示未读") @RequestParam(defaultValue = "false") boolean unreadOnly,
            @Parameter(description = "通知分类") @RequestParam(required = false) String category,
            @AuthenticationPrincipal User currentUser
    ) {
        List<NotificationService.NotificationData> notifications = notificationService.getUserNotifications(
                currentUser.getId(), limit, unreadOnly, category
        );

        List<Map<String, Object>> response = notifications.stream()
                .map(notification -> {
                    Map<String, Object> notifMap = new HashMap<>();
                    notifMap.put("id", notification.getId());
                    notifMap.put("title", notification.getTitle());
                    notifMap.put("content", notification.getContent());
                    notifMap.put("category", notification.getCategory());
                    notifMap.put("priority", notification.getPriority());
                    notifMap.put("isRead", notification.isRead());
                    notifMap.put("createdAt", notification.getCreatedAt());
                    notifMap.put("data", notification.getData());
                    notifMap.put("actionUrl", notification.getActionUrl());
                    return notifMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @Operation(summary = "获取通知摘要", description = "获取当前用户的通知统计信息")
    public ResponseEntity<Map<String, Object>> getNotificationSummary(
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> summary = notificationService.getNotificationSummary(currentUser.getId());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "发送通知给指定用户（管理员功能）")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal User currentUser
    ) {
        // 检查权限
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException("只有管理员可以发送通知");
        }

        Long userId = Long.valueOf(request.get("userId").toString());
        String templateId = (String) request.get("templateId");
        @SuppressWarnings("unchecked")
        Map<String, Object> customData = (Map<String, Object>) request.get("customData");
        String actionUrl = (String) request.get("actionUrl");

        NotificationService.NotificationData notification = notificationService.sendNotification(
                userId, templateId, customData, actionUrl
        );

        if (notification != null) {
            return ResponseEntity.ok(Map.of(
                    "message", "通知发送成功",
                    "notificationId", notification.getId()
            ));
        } else {
            throw new BusinessException("发送通知失败");
        }
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读")
    public ResponseEntity<Map<String, String>> markNotificationRead(
            @Parameter(description = "通知ID") @PathVariable Long notificationId,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean success = notificationService.markAsRead(notificationId, currentUser.getId());

        if (success) {
            return ResponseEntity.ok(Map.of("message", "标记成功"));
        } else {
            throw new BusinessException("通知不存在或无权访问");
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "获取通知分类列表", description = "获取所有可用的通知分类")
    public ResponseEntity<Map<String, Object>> getNotificationCategories() {
        List<Map<String, String>> categories = List.of(
                Map.of("id", "system", "name", "系统通知", "description", "系统维护、安全提醒等"),
                Map.of("id", "learning", "name", "学习通知", "description", "课程更新、任务提醒等"),
                Map.of("id", "social", "name", "社交通知", "description", "评论、点赞、关注等"),
                Map.of("id", "achievement", "name", "成就通知", "description", "等级提升、徽章获得等")
        );

        return ResponseEntity.ok(Map.of("categories", categories));
    }
} 