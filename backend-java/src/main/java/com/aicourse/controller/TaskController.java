package com.aicourse.controller;

import com.aicourse.entity.Task;
import com.aicourse.entity.User;
import com.aicourse.service.TaskService;
import com.aicourse.dto.TaskCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long student,
            @AuthenticationPrincipal User currentUser) {
        
        // 支持前端传递的type参数
        String actualTaskType = taskType != null ? taskType : type;
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Map<String, Object>> tasks = taskService.getTasks(
            pageable, courseId, actualTaskType, keyword, status, teacherId, student, currentUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", tasks.getContent());
        response.put("totalElements", tasks.getTotalElements());
        response.put("totalPages", tasks.getTotalPages());
        response.put("size", tasks.getSize());
        response.put("number", tasks.getNumber());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Map<String, Object>> getTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> taskDetail = taskService.getTaskDetail(taskId, currentUser);
        return ResponseEntity.ok(taskDetail);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Task> createTask(
            @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        Task task = taskService.createTask(request, currentUser);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> taskData,
            @AuthenticationPrincipal User currentUser) {
        
        Task task = taskService.updateTask(taskId, taskData, currentUser);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        taskService.deleteTask(taskId, currentUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "任务删除成功");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/submit")
    public ResponseEntity<Map<String, Object>> submitTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> submissionData,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> result = taskService.submitTask(taskId, submissionData, currentUser);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{taskId}/submissions")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskSubmissions(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> submissions = taskService.getTaskSubmissions(taskId, currentUser);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{taskId}/submissions/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskSubmissionStatistics(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> statistics = taskService.getTaskSubmissionStatistics(taskId, currentUser);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{taskId}/activities")
    public ResponseEntity<Map<String, Object>> getTaskActivities(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> activities = taskService.getTaskActivities(taskId, currentUser);
        return ResponseEntity.ok(activities);
    }

    @PostMapping("/{taskId}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> publishTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        taskService.publishTask(taskId, currentUser);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "任务发布成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskStatistics(
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> statistics = taskService.getTaskStatistics(courseId, currentUser);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{taskId}/analytics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTaskAnalytics(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User currentUser) {
        
        Map<String, Object> analytics = taskService.getTaskAnalytics(taskId, currentUser);
        return ResponseEntity.ok(analytics);
    }


} 