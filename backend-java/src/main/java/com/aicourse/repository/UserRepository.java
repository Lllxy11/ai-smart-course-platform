package com.aicourse.repository;

import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
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
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据角色查找用户
     */
    List<User> findByRole(UserRole role);

    /**
     * 根据角色查找用户（分页）
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * 根据激活状态查找用户
     */
    List<User> findByIsActive(Boolean isActive);

    /**
     * 根据激活状态查找用户（分页）
     */
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * 根据角色和激活状态查找用户
     */
    List<User> findByRoleAndIsActive(UserRole role, Boolean isActive);

    /**
     * 根据角色和激活状态查找用户（分页）
     */
    Page<User> findByRoleAndIsActive(UserRole role, Boolean isActive, Pageable pageable);

    /**
     * 根据班级ID查找学生
     */
    List<User> findByClassIdAndRole(Long classId, UserRole role);

    /**
     * 根据班级ID、角色和激活状态查找用户
     */
    List<User> findByClassIdAndRoleAndIsActiveTrue(Long classId, UserRole role);

    /**
     * 统计指定班级、角色和激活状态的用户数量
     */
    long countByClassIdAndRoleAndIsActiveTrue(Long classId, UserRole role);

    /**
     * 根据用户ID、班级ID和角色查找用户
     */
    User findByIdAndClassIdAndRole(Long id, Long classId, UserRole role);

    /**
     * 模糊搜索用户（用户名、全名、邮箱）
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.username LIKE %:keyword% OR " +
           "u.fullName LIKE %:keyword% OR " +
           "u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * 根据院系查找教师
     */
    List<User> findByDepartmentAndRole(String department, UserRole role);

    /**
     * 统计各角色用户数量
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * 统计激活用户数量
     */
    long countByIsActive(Boolean isActive);

    /**
     * 统计角色用户数量
     */
    Long countByRole(UserRole role);

    /**
     * 统计指定时间后创建的用户数量
     */
    Long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 根据用户名和ID查找（排除指定ID）
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.id != :id")
    Optional<User> findByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);

    /**
     * 根据邮箱和ID查找（排除指定ID）
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.id != :id")
    Optional<User> findByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    /**
     * 批量更新用户激活状态
     */
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id IN :ids")
    int updateIsActiveByIdIn(@Param("ids") List<Long> ids, @Param("isActive") Boolean isActive);

    /**
     * 批量删除用户
     */
    @Modifying
    void deleteByIdIn(List<Long> ids);
} 