package com.aicourse.controller;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @PostMapping("/{resourceId}/access")
    public ResponseEntity<Map<String, Object>> recordResourceAccess(@PathVariable String resourceId) {
        User currentUser = getCurrentUser();
        
        // 记录资源访问
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "访问记录已保存");
        result.put("resourceId", resourceId);
        result.put("userId", currentUser.getId());
        result.put("accessTime", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<Map<String, Object>> getResource(@PathVariable String resourceId) {
        User currentUser = getCurrentUser();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("resourceId", resourceId);
        result.put("title", "示例资源");
        result.put("type", "文档");
        result.put("url", "https://example.com/resource/" + resourceId);
        result.put("description", "这是一个示例学习资源");
        
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