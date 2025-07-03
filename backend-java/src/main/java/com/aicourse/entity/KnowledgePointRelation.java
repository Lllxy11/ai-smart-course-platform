package com.aicourse.entity;

import jakarta.persistence.*;

/**
 * 知识点关系实体类 - 优化版本
 */
@Entity
@Table(name = "knowledge_point_relations", indexes = {
    @Index(name = "idx_kpr_source", columnList = "source_id"),
    @Index(name = "idx_kpr_target", columnList = "target_id"),
    @Index(name = "idx_kpr_type", columnList = "relation_type")
})
public class KnowledgePointRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "relation_type", length = 50)
    private String relationType = "prerequisite";

    @Column(name = "strength")
    private Double strength = 1.0;

    // 构造函数
    public KnowledgePointRelation() {}

    public KnowledgePointRelation(Long sourceId, Long targetId, String relationType) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.relationType = relationType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Double getStrength() {
        return strength;
    }

    public void setStrength(Double strength) {
        this.strength = strength;
    }
} 