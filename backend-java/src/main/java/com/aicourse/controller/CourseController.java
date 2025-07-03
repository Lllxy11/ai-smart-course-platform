package com.aicourse.controller;

import com.aicourse.dto.*;
import com.aicourse.entity.User;
import com.aicourse.exception.BusinessException;
import com.aicourse.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 课程管理控制器
 */
@RestController
@RequestMapping("/courses")
@Tag(name = "课程管理", description = "课程相关API接口")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    @Operation(summary = "获取课程列表", description = "根据条件查询课程列表，支持分页和筛选")
    public ResponseEntity<PagedResponse<CourseResponse>> getCourses(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "课程分类") @RequestParam(required = false) String category,
            @Parameter(description = "难度等级") @RequestParam(required = false) String difficulty,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "学期") @RequestParam(required = false) String semester,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId,
            @AuthenticationPrincipal User currentUser
    ) {
        PagedResponse<CourseResponse> courses = courseService.getCourses(
                currentUser.getId(),
                currentUser.getRole(),
                page,
                size,
                category,
                difficulty,
                keyword,
                semester,
                status,
                teacherId
        );
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取课程统计信息", description = "获取课程统计数据")
    public ResponseEntity<Map<String, Object>> getCourseStatistics(
            @Parameter(description = "学期") @RequestParam(required = false) String semester,
            @Parameter(description = "年份") @RequestParam(required = false) Integer year,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> statistics = courseService.getCourseStatistics(
                currentUser.getId(), currentUser.getRole(), semester, year, category
        );
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "获取课程详情", description = "根据课程ID获取课程详细信息")
    public ResponseEntity<CourseResponse> getCourse(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseResponse course = courseService.getCourseById(courseId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(course);
    }

    @PostMapping
    @Operation(summary = "创建课程", description = "创建新的课程（仅教师和管理员）")
    public ResponseEntity<CourseResponse> createCourse(
            @Parameter(description = "课程创建请求") @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        // 权限检查：教师只能为自己创建课程，管理员可以为任何教师创建课程
        if (currentUser.getRole().name().equals("TEACHER") && !currentUser.getId().equals(request.getTeacherId())) {
            throw new BusinessException("教师只能为自己创建课程");
        }
        
        CourseResponse course = courseService.createCourse(request, request.getTeacherId());
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "更新课程", description = "更新课程信息（仅课程创建者和管理员）")
    public ResponseEntity<CourseResponse> updateCourse(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "课程更新请求") @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseResponse course = courseService.updateCourse(courseId, request, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "删除课程", description = "删除课程（仅课程创建者和管理员）")
    public ResponseEntity<Map<String, String>> deleteCourse(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        courseService.deleteCourse(courseId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of("message", "课程删除成功"));
    }

    @PostMapping("/{courseId}/enroll")
    @Operation(summary = "加入课程", description = "学生加入课程")
    public ResponseEntity<Map<String, Object>> enrollCourse(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        // 检查用户角色
        if (currentUser.getRole().name().equals("STUDENT")) {
            Map<String, Object> result = courseService.enrollCourse(courseId, currentUser.getId());
            return ResponseEntity.ok(result);
        } else {
            throw new BusinessException("只有学生可以选课");
        }
    }

    @GetMapping("/{courseId}/students")
    @Operation(summary = "获取课程学生列表", description = "获取课程的所有学生信息（仅教师和管理员）")
    public ResponseEntity<Map<String, Object>> getCourseStudents(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> students = courseService.getCourseStudents(courseId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{courseId}/progress")
    @Operation(summary = "获取课程学习进度", description = "获取当前用户在课程中的学习进度")
    public ResponseEntity<Map<String, Object>> getCourseProgress(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        // 这里可以添加获取学习进度的逻辑
        // 目前返回基本信息
        return ResponseEntity.ok(Map.of(
                "message", "获取学习进度成功",
                "courseId", courseId,
                "userId", currentUser.getId()
        ));
    }



    @GetMapping("/{courseId}/analytics")
    @Operation(summary = "获取课程分析数据", description = "获取课程的详细分析数据（仅教师和管理员）")
    public ResponseEntity<Map<String, Object>> getCourseAnalytics(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> analytics = courseService.getCourseAnalytics(courseId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(analytics);
    }
} 