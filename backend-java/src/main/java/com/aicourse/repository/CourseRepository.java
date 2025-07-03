package com.aicourse.repository;

import com.aicourse.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程数据访问接口
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * 根据教师ID查找课程
     */
    List<Course> findByTeacherId(Long teacherId);

    /**
     * 根据教师ID分页查找课程
     */
    Page<Course> findByTeacherId(Long teacherId, Pageable pageable);

    /**
     * 根据状态查找课程
     */
    List<Course> findByStatus(String status);

    /**
     * 根据分类查找课程
     */
    List<Course> findByCategory(String category);

    /**
     * 根据难度查找课程
     */
    List<Course> findByDifficulty(String difficulty);

    /**
     * 根据学期查找课程
     */
    List<Course> findBySemester(String semester);

    /**
     * 根据年份查找课程
     */
    List<Course> findByYear(Integer year);

    /**
     * 根据课程代码查找课程
     */
    Optional<Course> findByCode(String code);

    /**
     * 根据课程名称模糊查找
     */
    List<Course> findByNameContainingIgnoreCase(String name);

    /**
     * 查找激活的课程
     */
    List<Course> findByIsActiveTrue();

    /**
     * 查找激活的课程（分页）
     */
    Page<Course> findByIsActiveTrue(Pageable pageable);

    /**
     * 复合条件查询课程
     */
    @Query("SELECT c FROM Course c WHERE " +
           "(:teacherId IS NULL OR c.teacherId = :teacherId) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:difficulty IS NULL OR c.difficulty = :difficulty) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:semester IS NULL OR c.semester = :semester) AND " +
           "(:year IS NULL OR c.year = :year) AND " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "c.isActive = true")
    Page<Course> findCoursesWithFilters(
            @Param("teacherId") Long teacherId,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("status") String status,
            @Param("semester") String semester,
            @Param("year") Integer year,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 查找学生已选课程
     */
    @Query("SELECT c FROM Course c JOIN CourseEnrollment ce ON c.id = ce.courseId " +
           "WHERE ce.studentId = :studentId AND ce.isActive = true")
    List<Course> findEnrolledCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * 查找学生已选课程（分页）
     */
    @Query("SELECT c FROM Course c JOIN CourseEnrollment ce ON c.id = ce.courseId " +
           "WHERE ce.studentId = :studentId AND ce.isActive = true")
    Page<Course> findEnrolledCoursesByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * 统计教师的课程数量
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.teacherId = :teacherId")
    Long countByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 统计学生已选课程数量
     */
    @Query("SELECT COUNT(c) FROM Course c JOIN CourseEnrollment ce ON c.id = ce.courseId " +
           "WHERE ce.studentId = :studentId AND ce.isActive = true")
    Long countEnrolledCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * 查找热门课程（按选课人数排序）
     */
    @Query("SELECT c FROM Course c LEFT JOIN CourseEnrollment ce ON c.id = ce.courseId " +
           "WHERE c.isActive = true " +
           "GROUP BY c.id " +
           "ORDER BY COUNT(ce.id) DESC")
    Page<Course> findPopularCourses(Pageable pageable);

    /**
     * 查找推荐课程（相同分类或难度）
     */
    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "(c.category = :category OR c.difficulty = :difficulty) AND " +
           "c.id != :excludeCourseId " +
           "ORDER BY c.createdAt DESC")
    List<Course> findRecommendedCourses(
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("excludeCourseId") Long excludeCourseId,
            Pageable pageable
    );

    /**
     * 查找即将开始的课程
     */
    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "c.startDate > CURRENT_DATE " +
           "ORDER BY c.startDate ASC")
    List<Course> findUpcomingCourses(Pageable pageable);

    /**
     * 查找正在进行的课程
     */
    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "c.startDate <= CURRENT_DATE AND " +
           "(c.endDate IS NULL OR c.endDate >= CURRENT_DATE) " +
           "ORDER BY c.createdAt DESC")
    List<Course> findOngoingCourses(Pageable pageable);

    /**
     * 根据价格范围查找课程
     */
    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "c.price BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY c.price ASC")
    Page<Course> findCoursesByPriceRange(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    /**
     * 查找免费课程
     */
    @Query("SELECT c FROM Course c WHERE c.isActive = true AND " +
           "(c.price IS NULL OR c.price = 0) " +
           "ORDER BY c.createdAt DESC")
    Page<Course> findFreeCourses(Pageable pageable);

    /**
     * 统计学生选课数量
     */
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.studentId = :studentId AND ce.isActive = true")
    long countStudentCourses(@Param("studentId") Long studentId);

    /**
     * 统计课程的学生数量
     */
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.courseId = :courseId AND ce.isActive = true")
    long countStudentsByCourseId(@Param("courseId") Long courseId);
} 