package com.aicourse.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 知识点实体类 - 优化版本
 */
@Entity
@Table(name = "knowledge_points", indexes = {
    @Index(name = "idx_kp_course_id", columnList = "course_id"),
    @Index(name = "idx_kp_type", columnList = "point_type"),
    @Index(name = "idx_kp_course_type", columnList = "course_id, point_type"),
    @Index(name = "idx_kp_difficulty", columnList = "difficulty_level"),
    @Index(name = "idx_kp_parent", columnList = "parent_id")
})
public class KnowledgePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel = 1;

    @Column(name = "importance")
    private Double importance = 1.0;

    @Column(name = "estimated_time")
    private Integer estimatedTime = 0;

    @Column(name = "point_type", length = 50)
    private String pointType = "concept";

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public KnowledgePoint() {}

    public KnowledgePoint(String name, Long courseId) {
        this.name = name;
        this.courseId = courseId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Double getImportance() {
        return importance;
    }

    public void setImportance(Double importance) {
        this.importance = importance;
    }

    public Integer getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "KnowledgePoint{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pointType='" + pointType + '\'' +
                '}';
    }
} 