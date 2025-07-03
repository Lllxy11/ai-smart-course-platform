package com.aicourse.repository;

import com.aicourse.entity.Notification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通知Repository接口
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 按用户ID查询通知，按创建时间倒序
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, PageRequest pageRequest);
    
    /**
     * 按用户ID和已读状态查询通知，按创建时间倒序
     */
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, PageRequest pageRequest);
    
    /**
     * 按用户ID和分类查询通知，按创建时间倒序
     */
    List<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, String category, PageRequest pageRequest);
    
    /**
     * 按用户ID、分类和已读状态查询通知，按创建时间倒序
     */
    List<Notification> findByUserIdAndCategoryAndIsReadOrderByCreatedAtDesc(
            Long userId, String category, Boolean isRead, PageRequest pageRequest);
    
    /**
     * 按ID和用户ID查询通知
     */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 按ID和用户ID删除通知
     */
    int deleteByIdAndUserId(Long id, Long userId);
    
    /**
     * 统计用户在指定分类下的已读/未读通知数量
     */
    long countByUserIdAndCategoryAndIsRead(Long userId, String category, Boolean isRead);
    
    /**
     * 标记用户所有通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
    
    /**
     * 标记用户指定分类的所有通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.category = :category AND n.isRead = false")
    int markAllAsReadByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category, @Param("readAt") LocalDateTime readAt);
    
    /**
     * 统计用户未读通知总数
     */
    long countByUserIdAndIsRead(Long userId, Boolean isRead);
    
    /**
     * 查询用户最近的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByUserId(@Param("userId") Long userId, PageRequest pageRequest);
    
    /**
     * 查询指定时间范围内的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt BETWEEN :startTime AND :endTime ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询过期通知（用于清理）
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt < :expireTime")
    List<Notification> findExpiredNotifications(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 删除过期通知
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :expireTime")
    int deleteExpiredNotifications(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 按优先级查询通知
     */
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(Long userId, String priority, PageRequest pageRequest);
    
    /**
     * 查询用户所有分类的未读通知统计
     */
    @Query("SELECT n.category, COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false GROUP BY n.category")
    List<Object[]> countUnreadNotificationsByCategory(@Param("userId") Long userId);
} 