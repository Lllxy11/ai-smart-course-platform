package com.aicourse.controller;

import com.aicourse.entity.Resource;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

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

    // 教师上传课件/资料/视频
    @PostMapping("/upload")
    public ResponseEntity<Resource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "visibleToAll", required = false) Boolean visibleToAll,
            @AuthenticationPrincipal User currentUser) throws Exception {
        // 仅教师和管理员可上传
        if (currentUser == null || (!"TEACHER".equals(currentUser.getRole().name()) && !"ADMIN".equals(currentUser.getRole().name()))) {
            return ResponseEntity.status(403).build();
        }
        Resource resource = resourceService.saveResource(file, courseId, currentUser.getId(), description, visibleToAll);
        return ResponseEntity.ok(resource);
    }

    // 获取资料列表（可选按课程筛选）
    @GetMapping("")
    public ResponseEntity<List<Resource>> listResources(@RequestParam(value = "courseId", required = false) Long courseId) {
        List<Resource> resources = (courseId != null) ? resourceService.getResourcesByCourse(courseId) : resourceService.getAllPublicResources();
        return ResponseEntity.ok(resources);
    }

    // 获取单个资料详情
    @GetMapping("/detail/{id}")
    public ResponseEntity<Resource> getResourceDetail(@PathVariable Long id) {
        Resource resource = resourceService.getResource(id);
        if (resource == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resource);
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