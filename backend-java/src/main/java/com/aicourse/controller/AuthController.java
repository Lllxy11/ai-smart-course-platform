package com.aicourse.controller;

import com.aicourse.dto.LoginRequest;
import com.aicourse.dto.RegisterRequest;
import com.aicourse.dto.UserResponse;
import com.aicourse.service.AuthService;
import com.aicourse.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        Map<String, Object> response = authService.loginWithUserInfo(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账户")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        UserResponse response = authService.register(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        UserResponse response = authService.getCurrentUserInfo(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "更新用户信息", description = "更新当前用户的基本信息")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @RequestBody Map<String, Object> updates,
            @AuthenticationPrincipal User user) {
        UserResponse response = authService.updateUserInfo(user, updates);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        String currentPassword = request.get("current_password");
        String newPassword = request.get("new_password");
        
        authService.changePassword(user, currentPassword, newPassword);
        
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前用户登录状态")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal User user) {
        authService.logout(user);
        return ResponseEntity.ok(Map.of("message", "登出成功"));
    }

    @PostMapping("/logout-device")
    @Operation(summary = "退出指定设备", description = "退出指定设备的登录状态")
    public ResponseEntity<Map<String, String>> logoutDevice(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal User user) {
        Long deviceId = ((Number) request.get("device_id")).longValue();
        authService.logoutDevice(user, deviceId);
        return ResponseEntity.ok(Map.of("message", "设备已退出登录"));
    }

    @GetMapping("/login-devices")
    @Operation(summary = "获取登录设备列表", description = "获取当前用户的所有登录设备")
    public ResponseEntity<Map<String, Object>> getLoginDevices(@AuthenticationPrincipal User user) {
        Map<String, Object> response = authService.getLoginDevices(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "API健康状态检查")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }
} 