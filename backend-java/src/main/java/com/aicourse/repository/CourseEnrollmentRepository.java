package com.aicourse.repository;

import com.aicourse.entity.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 课程选课数据访问接口
 */
@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    /**
     * 根据学生ID查找选课记录
     */
    List<CourseEnrollment> findByStudentId(Long studentId);

    /**
     * 根据课程ID查找选课记录
     */
    List<CourseEnrollment> findByCourseId(Long courseId);

    /**
     * 根据学生ID和课程ID查找选课记录
     */
    Optional<CourseEnrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * 查找学生的激活选课记录
     */
    List<CourseEnrollment> findByStudentIdAndIsActiveTrue(Long studentId);

    /**
     * 查找课程的激活选课记录
     */
    List<CourseEnrollment> findByCourseIdAndIsActiveTrue(Long courseId);

    /**
     * 查找学生的激活选课记录（分页）
     */
    Page<CourseEnrollment> findByStudentIdAndIsActiveTrue(Long studentId, Pageable pageable);

    /**
     * 查找课程的激活选课记录（分页）
     */
    Page<CourseEnrollment> findByCourseIdAndIsActiveTrue(Long courseId, Pageable pageable);

    /**
     * 统计课程的选课人数
     */
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.courseId = :courseId AND ce.isActive = true")
    Long countByCourseIdAndIsActiveTrue(@Param("courseId") Long courseId);

    /**
     * 统计学生的选课数量
     */
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.studentId = :studentId AND ce.isActive = true")
    Long countByStudentIdAndIsActiveTrue(@Param("studentId") Long studentId);

    /**
     * 检查学生是否已选课
     */
    @Query("SELECT COUNT(ce) > 0 FROM CourseEnrollment ce WHERE " +
           "ce.studentId = :studentId AND ce.courseId = :courseId AND ce.isActive = true")
    boolean existsByStudentIdAndCourseIdAndIsActiveTrue(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId
    );

    /**
     * 查找学生在特定时间段内的选课记录
     */
    @Query("SELECT ce FROM CourseEnrollment ce WHERE " +
           "ce.studentId = :studentId AND ce.isActive = true AND " +
           "ce.enrolledAt BETWEEN :startTime AND :endTime")
    List<CourseEnrollment> findStudentEnrollmentsInPeriod(
            @Param("studentId") Long studentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查找课程在特定时间段内的选课记录
     */
    @Query("SELECT ce FROM CourseEnrollment ce WHERE " +
           "ce.courseId = :courseId AND ce.isActive = true AND " +
           "ce.enrolledAt BETWEEN :startTime AND :endTime")
    List<CourseEnrollment> findCourseEnrollmentsInPeriod(
            @Param("courseId") Long courseId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查找学生的完成课程
     */
    @Query("SELECT ce FROM CourseEnrollment ce WHERE " +
           "ce.studentId = :studentId AND ce.isActive = true AND " +
           "ce.progress >= 100")
    List<CourseEnrollment> findCompletedCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * 查找学生的进行中课程
     */
    @Query("SELECT ce FROM CourseEnrollment ce WHERE " +
           "ce.studentId = :studentId AND ce.isActive = true AND " +
           "ce.progress < 100")
    List<CourseEnrollment> findInProgressCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * 更新选课进度
     */
    @Modifying
    @Query("UPDATE CourseEnrollment ce SET ce.progress = :progress, ce.updatedAt = :updatedAt " +
           "WHERE ce.studentId = :studentId AND ce.courseId = :courseId")
    int updateProgress(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("progress") Integer progress,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * 更新学习时间
     */
    @Modifying
    @Query("UPDATE CourseEnrollment ce SET " +
           "ce.totalStudyTime = ce.totalStudyTime + :additionalTime, " +
           "ce.updatedAt = :updatedAt " +
           "WHERE ce.studentId = :studentId AND ce.courseId = :courseId")
    int updateStudyTime(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("additionalTime") Integer additionalTime,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * 更新最终成绩
     */
    @Modifying
    @Query("UPDATE CourseEnrollment ce SET ce.finalScore = :finalScore, ce.updatedAt = :updatedAt " +
           "WHERE ce.studentId = :studentId AND ce.courseId = :courseId")
    int updateFinalScore(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("finalScore") Double finalScore,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * 批量取消选课
     */
    @Modifying
    @Query("UPDATE CourseEnrollment ce SET ce.isActive = false, ce.updatedAt = :updatedAt " +
           "WHERE ce.courseId = :courseId")
    int batchUnenrollByCourseId(@Param("courseId") Long courseId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 查找热门课程统计
     */
    @Query("SELECT ce.courseId, COUNT(ce) as enrollmentCount FROM CourseEnrollment ce " +
           "WHERE ce.isActive = true " +
           "GROUP BY ce.courseId " +
           "ORDER BY enrollmentCount DESC")
    List<Object[]> findPopularCoursesStats(Pageable pageable);

    /**
     * 查找学生学习统计
     */
    @Query("SELECT ce.studentId, " +
           "COUNT(ce) as totalCourses, " +
           "AVG(ce.progress) as avgProgress, " +
           "SUM(ce.totalStudyTime) as totalStudyTime, " +
           "AVG(ce.finalScore) as avgScore " +
           "FROM CourseEnrollment ce " +
           "WHERE ce.isActive = true " +
           "GROUP BY ce.studentId")
    List<Object[]> findStudentLearningStats();

    /**
     * 查找特定学生的学习统计
     */
    @Query("SELECT " +
           "COUNT(ce) as totalCourses, " +
           "AVG(ce.progress) as avgProgress, " +
           "SUM(ce.totalStudyTime) as totalStudyTime, " +
           "AVG(ce.finalScore) as avgScore, " +
           "COUNT(CASE WHEN ce.progress >= 100 THEN 1 END) as completedCourses " +
           "FROM CourseEnrollment ce " +
           "WHERE ce.studentId = :studentId AND ce.isActive = true")
    Object[] findStudentLearningStatsByStudentId(@Param("studentId") Long studentId);

    /**
     * 查找课程的学习统计
     */
    @Query("SELECT " +
           "COUNT(ce) as totalStudents, " +
           "AVG(ce.progress) as avgProgress, " +
           "AVG(ce.totalStudyTime) as avgStudyTime, " +
           "AVG(ce.finalScore) as avgScore, " +
           "COUNT(CASE WHEN ce.progress >= 100 THEN 1 END) as completedStudents " +
           "FROM CourseEnrollment ce " +
           "WHERE ce.courseId = :courseId AND ce.isActive = true")
    Object[] findCourseLearningStatsByCourseId(@Param("courseId") Long courseId);
} 