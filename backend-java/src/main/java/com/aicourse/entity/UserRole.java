package com.aicourse.entity;

/**
 * 用户角色枚举
 */
public enum UserRole {
    TEACHER("teacher", "教师"),
    STUDENT("student", "学生"), 
    ADMIN("admin", "管理员");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
} 