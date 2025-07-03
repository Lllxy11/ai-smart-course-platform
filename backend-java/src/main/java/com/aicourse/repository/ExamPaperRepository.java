package com.aicourse.repository;

import com.aicourse.entity.ExamPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamPaperRepository extends JpaRepository<ExamPaper, Long> {
    
    // 根据课程ID查找考试
    List<ExamPaper> findByCourseId(Long courseId);
    
    // 根据创建者ID查找考试
    List<ExamPaper> findByCreatorId(Long creatorId);
    
    // 根据状态查找考试
    List<ExamPaper> findByStatus(String status);
    
    // 根据课程ID和状态查找考试
    List<ExamPaper> findByCourseIdAndStatus(Long courseId, String status);
    
    // 查找已发布的考试
    List<ExamPaper> findByStatusOrderByCreatedAtDesc(String status);
    
    // 查找正在进行的考试
    @Query("SELECT e FROM ExamPaper e WHERE e.status = 'published' AND " +
           "(e.startTime IS NULL OR e.startTime <= :now) AND " +
           "(e.endTime IS NULL OR e.endTime >= :now)")
    List<ExamPaper> findActiveExams(@Param("now") LocalDateTime now);
    
    // 根据标题模糊查询
    List<ExamPaper> findByTitleContaining(String keyword);
    
    // 统计某课程的考试数量
    long countByCourseId(Long courseId);
    
    // 统计某用户创建的考试数量
    long countByCreatorId(Long creatorId);
    
    // 统计已发布的考试数量
    long countByStatus(String status);
    
    // 获取最近创建的考试
    @Query("SELECT e FROM ExamPaper e ORDER BY e.createdAt DESC")
    List<ExamPaper> findRecentExams();
} 