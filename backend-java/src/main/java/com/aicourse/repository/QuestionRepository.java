package com.aicourse.repository;

import com.aicourse.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    
    // 根据课程ID查找题目
    List<Question> findByCourseId(Long courseId);
    
    // 根据题目类型查找题目
    List<Question> findByType(String type);
    
    // 根据难度查找题目
    List<Question> findByDifficulty(String difficulty);
    
    // 根据创建者ID查找题目
    List<Question> findByCreatedBy(Long createdBy);
    
    // 查找某课程的题目数量
    long countByCourseId(Long courseId);
    
    // 根据内容模糊查询
    List<Question> findByContentContaining(String keyword);
    
    // 查找某类型和难度的题目
    List<Question> findByTypeAndDifficulty(String type, String difficulty);
    
    // 获取随机题目
    @Query(value = "SELECT * FROM questions WHERE course_id = :courseId ORDER BY RANDOM() LIMIT :limit", 
           nativeQuery = true)
    List<Question> findRandomQuestions(@Param("courseId") Long courseId, @Param("limit") int limit);
    
    // 通过考试ID查找题目（需要连接exam_questions表）
    @Query("SELECT q FROM Question q " +
           "JOIN ExamQuestion eq ON q.id = eq.questionId " +
           "WHERE eq.examPaperId = :examPaperId " +
           "ORDER BY eq.orderIndex ASC")
    List<Question> findQuestionsByExamPaperId(@Param("examPaperId") Long examPaperId);
    
    // 统计某考试的题目数量（需要连接exam_questions表）
    @Query("SELECT COUNT(q) FROM Question q " +
           "JOIN ExamQuestion eq ON q.id = eq.questionId " +
           "WHERE eq.examPaperId = :examPaperId")
    long countQuestionsByExamPaperId(@Param("examPaperId") Long examPaperId);
} 