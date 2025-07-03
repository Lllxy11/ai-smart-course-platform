package com.aicourse.repository;

import com.aicourse.entity.KnowledgePointRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgePointRelationRepository extends JpaRepository<KnowledgePointRelation, Long> {
    
    // 批量查询关系 - 关键优化
    @Query("SELECT r FROM KnowledgePointRelation r WHERE r.sourceId IN :nodeIds OR r.targetId IN :nodeIds")
    List<KnowledgePointRelation> findRelationsByNodeIds(@Param("nodeIds") List<Long> nodeIds);
    
    // 获取指定节点的前置关系
    List<KnowledgePointRelation> findByTargetIdAndRelationType(Long targetId, String relationType);
    
    // 获取指定节点的后继关系
    List<KnowledgePointRelation> findBySourceIdAndRelationType(Long sourceId, String relationType);
} 