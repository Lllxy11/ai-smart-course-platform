package com.aicourse.controller;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.service.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 知识点控制器 - 高性能版本
 */
@RestController
@RequestMapping("/knowledge-points")
@CrossOrigin(origins = "*")
public class KnowledgePointController {

    @Autowired
    private KnowledgePointService knowledgePointService;

    /**
     * 获取知识图谱数据 - 主要API
     */
    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getKnowledgeGraph(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String pointType) {
        
        User currentUser = getCurrentUser();
        Map<String, Object> graph = knowledgePointService.getKnowledgeGraph(courseId, pointType, currentUser);
        return ResponseEntity.ok(graph);
    }

    /**
     * 分页获取知识点列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getKnowledgePoints(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> result = knowledgePointService.getKnowledgePointsPaged(courseId, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取知识点树形结构
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getKnowledgeTree(
            @RequestParam Long courseId) {
        
        var tree = knowledgePointService.getKnowledgeTree(courseId);
        return ResponseEntity.ok(Map.of("tree", tree));
    }

    /**
     * 获取知识点统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) Long courseId) {
        
        Map<String, Object> stats = knowledgePointService.getStatistics(courseId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 创建知识点
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createKnowledgePoint(
            @RequestBody Map<String, Object> pointData) {
        
        try {
            User currentUser = getCurrentUser();
            Map<String, Object> result = knowledgePointService.createKnowledgePoint(pointData, currentUser);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "创建知识点失败: " + e.getMessage()));
        }
    }

    /**
     * 更新知识点
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateKnowledgePoint(
            @PathVariable Long id,
            @RequestBody Map<String, Object> pointData) {
        
        try {
            User currentUser = getCurrentUser();
            Map<String, Object> result = knowledgePointService.updateKnowledgePoint(id, pointData, currentUser);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "更新知识点失败: " + e.getMessage()));
        }
    }

    /**
     * 删除知识点
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteKnowledgePoint(@PathVariable Long id) {
        
        try {
            User currentUser = getCurrentUser();
            knowledgePointService.deleteKnowledgePoint(id, currentUser);
            return ResponseEntity.ok(Map.of("message", "知识点删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "删除知识点失败: " + e.getMessage()));
        }
    }

    /**
     * 获取知识点详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getKnowledgePointById(@PathVariable Long id) {
        
        try {
            Map<String, Object> result = knowledgePointService.getKnowledgePointById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取知识点关系
     */
    @GetMapping("/{id}/relations")
    public ResponseEntity<Map<String, Object>> getKnowledgePointRelations(@PathVariable Long id) {
        
        try {
            Map<String, Object> result = knowledgePointService.getKnowledgePointRelations(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "获取关系失败: " + e.getMessage()));
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