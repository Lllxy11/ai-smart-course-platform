package com.aicourse.service;

import com.aicourse.dto.*;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户业务服务
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 获取用户列表 (只读操作)
     */
    @Transactional(readOnly = true, timeout = 30)
    public PagedResponse<UserResponse> getUsers(
            int page, int size, UserRole role, Boolean isActive, Long currentUserId, UserRole currentUserRole
    ) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 前端传入的page是从1开始的，需要减1
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (role != null && isActive != null) {
            userPage = userRepository.findByRoleAndIsActive(role, isActive, pageable);
        } else if (role != null) {
            userPage = userRepository.findByRole(role, pageable);
        } else if (isActive != null) {
            userPage = userRepository.findByIsActive(isActive, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::buildUserResponse)
                .collect(Collectors.toList());

        // 返回给前端的页码保持不变（前端传入的是从1开始的页码）
        return PagedResponse.of(userResponses, userPage.getTotalElements(), page, size);
    }

    /**
     * 获取用户统计信息 (只读操作)
     */
    @Transactional(readOnly = true, timeout = 30)
    public Map<String, Object> getUserStatistics(Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 计算总用户数
        Long totalUsers = userRepository.count();
        
        // 计算活跃用户数
        Long activeUsers = userRepository.countByIsActive(true);
        
        // 计算今日新增用户数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long newUsersToday = userRepository.countByCreatedAtAfter(todayStart);
        
        // 计算在线用户数（这里暂时使用活跃用户数的一半作为模拟值）
        Long onlineUsers = activeUsers / 2;
        
        // 额外统计信息
        Long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        Long teachers = userRepository.countByRole(UserRole.TEACHER);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
        Long newThisMonth = userRepository.countByCreatedAtAfter(oneMonthAgo);

        Map<String, Object> stats = new HashMap<>();
        // 前端期望的字段
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("newUsersToday", newUsersToday);
        stats.put("onlineUsers", onlineUsers);
        
        // 额外的统计信息
        stats.put("totalStudents", totalStudents);
        stats.put("teachers", teachers);
        stats.put("newThisMonth", newThisMonth);

        return stats;
    }

    /**
     * 获取用户详情 (只读操作)
     */
    @Transactional(readOnly = true, timeout = 15)
    public UserResponse getUserById(Long userId, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN && !currentUserId.equals(userId)) {
            throw new BusinessException("权限不足");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        return buildUserResponse(user);
    }

    /**
     * 创建用户 (写操作)
     */
    @Transactional(timeout = 30, isolation = Isolation.SERIALIZABLE)
    public UserResponse createUser(UserCreateRequest request, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 检查用户名和邮箱唯一性
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setStudentId(request.getStudentId());
        user.setMajor(request.getMajor());
        user.setGrade(request.getGrade());
        user.setClassName(request.getClassName());
        user.setTeacherId(request.getTeacherId());
        user.setDepartment(request.getDepartment());
        user.setTitle(request.getTitle());
        user.setBio(request.getBio());
        user.setIsActive(request.getIsActive());
        user.setIsVerified(request.getIsVerified());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        return buildUserResponse(user);
    }

    /**
     * 更新用户 (写操作)
     */
    @Transactional(timeout = 30, isolation = Isolation.SERIALIZABLE)
    public UserResponse updateUser(
            Long userId, UserUpdateRequest request, Long currentUserId, UserRole currentUserRole
    ) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN && !currentUserId.equals(userId)) {
            throw new BusinessException("权限不足");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        // 检查用户名唯一性
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsernameAndIdNot(request.getUsername(), userId).isPresent()) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 检查邮箱唯一性
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmailAndIdNot(request.getEmail(), userId).isPresent()) {
                throw new BusinessException("邮箱已存在");
            }
        }

        // 更新字段
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getStudentId() != null) user.setStudentId(request.getStudentId());
        if (request.getMajor() != null) user.setMajor(request.getMajor());
        if (request.getGrade() != null) user.setGrade(request.getGrade());
        if (request.getClassName() != null) user.setClassName(request.getClassName());
        if (request.getTeacherId() != null) user.setTeacherId(request.getTeacherId());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getTitle() != null) user.setTitle(request.getTitle());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());
        if (request.getIsVerified() != null) user.setIsVerified(request.getIsVerified());

        // 更新密码
        if (request.getPassword() != null) {
            user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        return buildUserResponse(user);
    }

    /**
     * 删除用户 (写操作)
     */
    @Transactional(timeout = 30, isolation = Isolation.SERIALIZABLE)
    public void deleteUser(Long userId, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 不能删除自己
        if (currentUserId.equals(userId)) {
            throw new BusinessException("不能删除自己");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        userRepository.delete(user);
    }

    /**
     * 激活用户 (写操作)
     */
    @Transactional(timeout = 15, isolation = Isolation.SERIALIZABLE)
    public void activateUser(Long userId, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 禁用用户 (写操作)
     */
    @Transactional(timeout = 15, isolation = Isolation.SERIALIZABLE)
    public void deactivateUser(Long userId, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 不能禁用自己
        if (currentUserId.equals(userId)) {
            throw new BusinessException("不能禁用自己");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 重置用户密码 (写操作)
     */
    @Transactional(timeout = 15, isolation = Isolation.SERIALIZABLE)
    public void resetUserPassword(Long userId, String newPassword, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 批量激活用户 (写操作)
     */
    @Transactional(timeout = 60, isolation = Isolation.SERIALIZABLE)
    public int batchActivateUsers(List<Long> userIds, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        int result = userRepository.updateIsActiveByIdIn(userIds, true);
        entityManager.flush();
        return result;
    }

    /**
     * 批量禁用用户 (写操作)
     */
    @Transactional(timeout = 60, isolation = Isolation.SERIALIZABLE)
    public int batchDeactivateUsers(List<Long> userIds, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 排除当前用户
        userIds = userIds.stream()
                .filter(id -> !id.equals(currentUserId))
                .collect(Collectors.toList());

        int result = userRepository.updateIsActiveByIdIn(userIds, false);
        entityManager.flush();
        return result;
    }

    /**
     * 批量删除用户 (写操作)
     */
    @Transactional(timeout = 60, isolation = Isolation.SERIALIZABLE)
    public int batchDeleteUsers(List<Long> userIds, Long currentUserId, UserRole currentUserRole) {
        // 检查权限
        if (currentUserRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 排除当前用户
        userIds = userIds.stream()
                .filter(id -> !id.equals(currentUserId))
                .collect(Collectors.toList());

        userRepository.deleteByIdIn(userIds);
        return userIds.size();
    }

    /**
     * 构建用户响应DTO (私有方法，无需事务)
     */
    private UserResponse buildUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setPhone(user.getPhone());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setStudentId(user.getStudentId());
        response.setMajor(user.getMajor());
        response.setGrade(user.getGrade());
        response.setClassName(user.getClassName());
        response.setTeacherId(user.getTeacherId());
        response.setDepartment(user.getDepartment());
        response.setTitle(user.getTitle());
        response.setBio(user.getBio());
        response.setIsActive(user.getIsActive());
        response.setIsVerified(user.getIsVerified());
        response.setLastLogin(user.getLastLogin());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
} 