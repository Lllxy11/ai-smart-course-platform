package com.aicourse.repository;

import com.aicourse.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户会话数据访问接口
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * 根据会话令牌查找会话
     */
    Optional<UserSession> findBySessionToken(String sessionToken);

    /**
     * 根据用户ID查找活跃会话
     */
    List<UserSession> findByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * 根据用户ID查找当前会话
     */
    Optional<UserSession> findByUserIdAndIsCurrent(Long userId, Boolean isCurrent);

    /**
     * 根据用户ID查找所有会话（按最后活动时间排序）
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId ORDER BY s.lastActivity DESC")
    List<UserSession> findByUserIdOrderByLastActivityDesc(@Param("userId") Long userId);

    /**
     * 将用户的其他会话设为非当前
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isCurrent = false WHERE s.userId = :userId AND s.isActive = true")
    void setOtherSessionsAsNotCurrent(@Param("userId") Long userId);

    /**
     * 将指定会话设为非活跃
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.isCurrent = false WHERE s.id = :sessionId AND s.userId = :userId")
    void deactivateSession(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 将用户所有会话设为非活跃
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.isCurrent = false WHERE s.userId = :userId")
    void deactivateAllUserSessions(@Param("userId") Long userId);

    /**
     * 删除过期会话
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * 统计用户活跃会话数量
     */
    long countByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * 根据IP地址查找会话
     */
    List<UserSession> findByIpAddress(String ipAddress);

    /**
     * 查找指定时间后的活跃会话
     */
    @Query("SELECT s FROM UserSession s WHERE s.lastActivity > :after AND s.isActive = true")
    List<UserSession> findActiveSessionsAfter(@Param("after") LocalDateTime after);
} 