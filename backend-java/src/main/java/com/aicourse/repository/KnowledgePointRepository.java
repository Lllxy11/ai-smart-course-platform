package com.aicourse.repository;

import com.aicourse.entity.KnowledgePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {
    
    // 基础查询 - 已建立索引
    List<KnowledgePoint> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    
    List<KnowledgePoint> findByCourseIdAndPointTypeOrderByOrderIndexAsc(Long courseId, String pointType);
    
    // 批量查询 - 关键优化
    @Query("SELECT kp FROM KnowledgePoint kp WHERE kp.courseId IN :courseIds ORDER BY kp.courseId, kp.orderIndex")
    List<KnowledgePoint> findByMultipleCourses(@Param("courseIds") List<Long> courseIds);
    
    // 带分页的高效查询
    @Query(value = "SELECT * FROM knowledge_points WHERE course_id = :courseId " +
           "ORDER BY order_index LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<KnowledgePoint> findByCourseIdWithPagination(@Param("courseId") Long courseId, 
                                                      @Param("limit") int limit, 
                                                      @Param("offset") int offset);
    
    // 统计查询
    long countByCourseId(Long courseId);
    
    long countByCourseIdAndPointType(Long courseId, String pointType);
    
    // 树形结构查询
    List<KnowledgePoint> findByParentIdIsNullAndCourseIdOrderByOrderIndexAsc(Long courseId);
    
    List<KnowledgePoint> findByParentIdOrderByOrderIndexAsc(Long parentId);
} 