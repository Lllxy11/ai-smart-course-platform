package com.aicourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 密码修改请求DTO
 */
public class PasswordChangeRequest {

    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    public PasswordChangeRequest() {}

    public PasswordChangeRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // 验证新密码和确认密码是否一致
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    @Override
    public String toString() {
        return "PasswordChangeRequest{" +
                "hasCurrentPassword=" + (currentPassword != null && !currentPassword.isEmpty()) +
                ", hasNewPassword=" + (newPassword != null && !newPassword.isEmpty()) +
                ", hasConfirmPassword=" + (confirmPassword != null && !confirmPassword.isEmpty()) +
                ", passwordMatching=" + isPasswordMatching() +
                '}';
    }
} 