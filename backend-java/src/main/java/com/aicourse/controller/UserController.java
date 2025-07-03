package com.aicourse.controller;

import com.aicourse.dto.*;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户相关API接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取用户列表，支持分页和筛选（仅管理员）")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "用户角色") @RequestParam(required = false) UserRole role,
            @Parameter(description = "激活状态") @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal User currentUser
    ) {
        PagedResponse<UserResponse> users = userService.getUsers(
                page, size, role, isActive, currentUser.getId(), currentUser.getRole()
        );
        return ResponseEntity.ok(users);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计信息", description = "获取用户统计数据（仅管理员）")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> statistics = userService.getUserStatistics(
                currentUser.getId(), currentUser.getRole()
        );
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        UserResponse user = userService.getUserById(userId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户（仅管理员）")
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "用户创建请求") @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        UserResponse user = userService.createUser(request, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "用户更新请求") @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        UserResponse user = userService.updateUser(userId, request, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "删除用户（仅管理员）")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.deleteUser(userId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of("message", "用户删除成功"));
    }

    @PostMapping("/{userId}/activate")
    @Operation(summary = "激活用户", description = "激活指定用户（仅管理员）")
    public ResponseEntity<Map<String, String>> activateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.activateUser(userId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of("message", "用户激活成功"));
    }

    @PostMapping("/{userId}/deactivate")
    @Operation(summary = "禁用用户", description = "禁用指定用户（仅管理员）")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        userService.deactivateUser(userId, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of("message", "用户禁用成功"));
    }

    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "重置用户密码", description = "重置指定用户的密码（仅管理员）")
    public ResponseEntity<Map<String, String>> resetUserPassword(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "新密码") @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User currentUser
    ) {
        String newPassword = request.get("newPassword");
        userService.resetUserPassword(userId, newPassword, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of("message", "密码重置成功"));
    }

    @PostMapping("/batch/activate")
    @Operation(summary = "批量激活用户", description = "批量激活多个用户（仅管理员）")
    public ResponseEntity<Map<String, Object>> batchActivateUsers(
            @Parameter(description = "用户ID列表") @RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal User currentUser
    ) {
        List<Long> userIds = request.get("userIds");
        int count = userService.batchActivateUsers(userIds, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of(
                "message", String.format("成功激活 %d 个用户", count),
                "count", count
        ));
    }

    @PostMapping("/batch/deactivate")
    @Operation(summary = "批量禁用用户", description = "批量禁用多个用户（仅管理员）")
    public ResponseEntity<Map<String, Object>> batchDeactivateUsers(
            @Parameter(description = "用户ID列表") @RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal User currentUser
    ) {
        List<Long> userIds = request.get("userIds");
        int count = userService.batchDeactivateUsers(userIds, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of(
                "message", String.format("成功禁用 %d 个用户", count),
                "count", count
        ));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户", description = "批量删除多个用户（仅管理员）")
    public ResponseEntity<Map<String, Object>> batchDeleteUsers(
            @Parameter(description = "用户ID列表") @RequestBody Map<String, List<Long>> request,
            @AuthenticationPrincipal User currentUser
    ) {
        List<Long> userIds = request.get("userIds");
        int count = userService.batchDeleteUsers(userIds, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.ok(Map.of(
                "message", String.format("成功删除 %d 个用户", count),
                "count", count
        ));
    }
} 