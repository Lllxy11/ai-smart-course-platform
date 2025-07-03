package com.aicourse.repository;

import com.aicourse.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long>, JpaSpecificationExecutor<Classroom> {
    
    // 根据班级代码查找班级
    Optional<Classroom> findByCode(String code);
    
    // 根据班级代码查找活跃班级
    Optional<Classroom> findByCodeAndIsActiveTrue(String code);
    
    // 查找所有活跃班级
    List<Classroom> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // 根据年级查找班级
    List<Classroom> findByGradeLevelAndIsActiveTrueOrderByNameAsc(String gradeLevel);
    
    // 根据专业查找班级
    List<Classroom> findByMajorAndIsActiveTrueOrderByNameAsc(String major);
    
    // 根据班主任ID查找班级
    List<Classroom> findByAdvisorIdAndIsActiveTrueOrderByNameAsc(Long advisorId);
    
    // 根据年级和专业查找班级
    List<Classroom> findByGradeLevelAndMajorAndIsActiveTrueOrderByNameAsc(String gradeLevel, String major);
    
    // 检查班级代码是否已存在（排除指定ID）
    boolean existsByCodeAndIdNot(String code, Long id);
    
    // 检查班级代码是否已存在
    boolean existsByCode(String code);
    
    // 模糊搜索班级（班级名称、代码、专业）
    @Query("SELECT c FROM Classroom c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.major) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.createdAt DESC")
    List<Classroom> findBySearchKeyword(@Param("search") String search);
    
    // 统计班级数量按年级分组
    @Query("SELECT c.gradeLevel, COUNT(c) FROM Classroom c WHERE c.isActive = true GROUP BY c.gradeLevel")
    List<Object[]> countByGradeLevel();
    
    // 统计班级数量按专业分组
    @Query("SELECT c.major, COUNT(c) FROM Classroom c WHERE c.isActive = true GROUP BY c.major")
    List<Object[]> countByMajor();
    
    // 查找学生人数不足的班级
    @Query("SELECT c FROM Classroom c WHERE c.isActive = true AND c.studentCount < c.maxStudents ORDER BY c.studentCount ASC")
    List<Classroom> findClassroomsWithAvailableSpace();
    
    // 查找学生人数已满的班级
    @Query("SELECT c FROM Classroom c WHERE c.isActive = true AND c.studentCount >= c.maxStudents ORDER BY c.name ASC")
    List<Classroom> findFullClassrooms();
    
    // 统计总班级数（活跃的）
    long countByIsActiveTrue();
    
    // 统计总学生数（所有活跃班级）
    @Query("SELECT SUM(c.studentCount) FROM Classroom c WHERE c.isActive = true")
    Long getTotalStudentsInActiveClasses();
} 