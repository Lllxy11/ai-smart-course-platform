package com.aicourse.repository;

import com.aicourse.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务数据访问接口
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * 根据课程ID查找任务
     */
    List<Task> findByCourseId(Long courseId);

    /**
     * 根据创建者ID查找任务
     */
    List<Task> findByCreatorId(Long creatorId);

    /**
     * 根据状态查找任务
     */
    List<Task> findByStatus(String status);

    /**
     * 根据任务类型查找任务
     */
    List<Task> findByTaskType(String taskType);

    /**
     * 根据课程ID和状态查找任务
     */
    List<Task> findByCourseIdAndStatus(Long courseId, String status);

    /**
     * 根据创建者ID和课程ID查找任务
     */
    List<Task> findByCreatorIdAndCourseId(Long creatorId, Long courseId);

    /**
     * 查找已发布的任务
     */
    List<Task> findByIsPublishedTrue();

    /**
     * 根据标题模糊查询
     */
    List<Task> findByTitleContaining(String keyword);

    /**
     * 复合查询：根据课程ID、任务类型和状态查找
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(:courseId IS NULL OR t.courseId = :courseId) AND " +
           "(:taskType IS NULL OR t.taskType = :taskType) AND " +
           "(:status IS NULL OR t.status = :status)")
    List<Task> findTasksByFilters(@Param("courseId") Long courseId, 
                                  @Param("taskType") String taskType, 
                                  @Param("status") String status);

    /**
     * 统计某课程的任务数量
     */
    long countByCourseId(Long courseId);

    /**
     * 统计某用户创建的任务数量
     */
    long countByCreatorId(Long creatorId);

    /**
     * 统计已发布的任务数量
     */
    long countByIsPublishedTrue();
} 