package com.aicourse.repository;

import com.aicourse.entity.LearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {
    
    // 根据学生ID查找学习记录
    List<LearningRecord> findByStudentId(Long studentId);
    
    // 根据学生ID和课程ID查找学习记录
    List<LearningRecord> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    // 根据学生ID和任务ID查找学习记录
    List<LearningRecord> findByStudentIdAndTaskId(Long studentId, Long taskId);
    
    // 根据行为类型查找学习记录
    List<LearningRecord> findByStudentIdAndActionType(Long studentId, LearningRecord.ActionType actionType);
    
    // 根据学生ID和知识点ID查找学习记录
    List<LearningRecord> findByStudentIdAndKnowledgePointId(Long studentId, Long knowledgePointId);
    
    // 根据学生ID、知识点ID和行为类型查找学习记录
    List<LearningRecord> findByStudentIdAndKnowledgePointIdAndActionType(Long studentId, Long knowledgePointId, LearningRecord.ActionType actionType);
    
    // 根据知识点ID查找所有学习记录
    List<LearningRecord> findByKnowledgePointId(Long knowledgePointId);
    
    // 查找学生最近的学习记录
    @Query("SELECT lr FROM LearningRecord lr WHERE lr.studentId = :studentId ORDER BY lr.createdAt DESC")
    List<LearningRecord> findRecentByStudentId(@Param("studentId") Long studentId);
    
    // 查找学生在指定时间范围内的学习记录
    @Query("SELECT lr FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.createdAt BETWEEN :startTime AND :endTime ORDER BY lr.createdAt DESC")
    List<LearningRecord> findByStudentIdAndTimeRange(@Param("studentId") Long studentId, 
                                                     @Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);
    
    // 计算学生总学习时长
    @Query("SELECT COALESCE(SUM(lr.duration), 0) FROM LearningRecord lr WHERE lr.studentId = :studentId")
    Long calculateTotalStudyTime(@Param("studentId") Long studentId);
    
    // 计算学生在指定课程的学习时长
    @Query("SELECT COALESCE(SUM(lr.duration), 0) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.courseId = :courseId")
    Long calculateStudyTimeByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    // 计算学生在指定任务的学习时长
    @Query("SELECT COALESCE(SUM(lr.duration), 0) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.taskId = :taskId")
    Long calculateStudyTimeByTask(@Param("studentId") Long studentId, @Param("taskId") Long taskId);
    
    // 计算学生在指定知识点的学习时长
    @Query("SELECT COALESCE(SUM(lr.duration), 0) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.knowledgePointId = :knowledgePointId")
    Long calculateStudyTimeByKnowledgePoint(@Param("studentId") Long studentId, @Param("knowledgePointId") Long knowledgePointId);
    
    // 获取学生最后一次学习指定课程的时间
    @Query("SELECT MAX(lr.createdAt) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.courseId = :courseId")
    LocalDateTime findLastStudyTimeByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    // 获取学生最后一次学习指定任务的时间
    @Query("SELECT MAX(lr.createdAt) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.taskId = :taskId")
    LocalDateTime findLastStudyTimeByTask(@Param("studentId") Long studentId, @Param("taskId") Long taskId);
    
    // 获取学生最后一次学习指定知识点的时间
    @Query("SELECT MAX(lr.learningTime) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.knowledgePointId = :knowledgePointId")
    LocalDateTime findLastStudyTimeByKnowledgePoint(@Param("studentId") Long studentId, @Param("knowledgePointId") Long knowledgePointId);
    
    // 统计学生在指定时间范围内的学习天数
    @Query("SELECT COUNT(DISTINCT DATE(lr.createdAt)) FROM LearningRecord lr WHERE lr.studentId = :studentId AND lr.createdAt BETWEEN :startTime AND :endTime")
    Long countStudyDays(@Param("studentId") Long studentId, 
                       @Param("startTime") LocalDateTime startTime, 
                       @Param("endTime") LocalDateTime endTime);
} 