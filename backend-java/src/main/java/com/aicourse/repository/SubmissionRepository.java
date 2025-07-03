package com.aicourse.repository;

import com.aicourse.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {
    
    // 根据任务ID查找提交
    List<Submission> findByTaskId(Long taskId);
    
    // 根据学生ID查找提交
    List<Submission> findByStudentId(Long studentId);
    
    // 根据状态查找提交
    List<Submission> findByStatus(Submission.SubmissionStatus status);
    
    // 根据任务ID和学生ID查找提交
    List<Submission> findByTaskIdAndStudentId(Long taskId, Long studentId);
    
    // 查找已评分的提交
    @Query("SELECT s FROM Submission s WHERE s.score IS NOT NULL")
    List<Submission> findGradedSubmissions();
    
    // 查找待评分的提交
    @Query("SELECT s FROM Submission s WHERE s.score IS NULL AND s.submittedAt IS NOT NULL")
    List<Submission> findPendingGradeSubmissions();
    
    // 根据课程ID查找提交（通过任务关联）
    @Query("SELECT s FROM Submission s JOIN s.task t WHERE t.course.id = :courseId")
    List<Submission> findByCourseId(@Param("courseId") Long courseId);
    
    // 根据创建者ID查找提交（通过任务关联）
    @Query("SELECT s FROM Submission s JOIN s.task t WHERE t.creator.id = :creatorId")
    List<Submission> findByTaskCreatorId(@Param("creatorId") Long creatorId);
    
    // 统计某任务的提交数量
    long countByTaskId(Long taskId);
    
    // 统计某学生的提交数量
    long countByStudentId(Long studentId);
    
    // 统计已评分的提交数量
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.score IS NOT NULL")
    long countGradedSubmissions();
    
    // 获取某任务的平均分
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.taskId = :taskId AND s.score IS NOT NULL")
    Double getAverageScoreByTaskId(@Param("taskId") Long taskId);
    
    // 根据课程ID查找已评分的提交
    @Query("SELECT s FROM Submission s JOIN s.task t WHERE t.course.id = :courseId AND s.score IS NOT NULL")
    List<Submission> findGradedSubmissionsByCourse(@Param("courseId") Long courseId);
    
    // 根据任务ID查找已评分的提交
    @Query("SELECT s FROM Submission s WHERE s.taskId = :taskId AND s.score IS NOT NULL")
    List<Submission> findGradedSubmissionsByTask(@Param("taskId") Long taskId);
    
    // 根据学生ID和课程ID查找已评分的提交
    @Query("SELECT s FROM Submission s JOIN s.task t WHERE s.studentId = :studentId AND (:courseId IS NULL OR t.course.id = :courseId) AND s.score IS NOT NULL")
    List<Submission> findGradedSubmissionsByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
} 