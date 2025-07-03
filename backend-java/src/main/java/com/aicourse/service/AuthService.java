package com.aicourse.service;

import com.aicourse.dto.*;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.repository.UserRepository;
import com.aicourse.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 认证服务实现类
 * 提供用户认证、注册、密码管理等功能
 * 
 * @author AI Smart Course Platform
 * @version 1.0
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    // 密码复杂度正则表达式
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    
    // 邮箱格式正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    // 用户名格式正则表达式（只允许字母、数字、下划线）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @param httpRequest HTTP请求对象
     * @return JWT Token响应
     * @throws BusinessException 登录失败时抛出
     */
    public TokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        logger.info("用户登录请求开始: username={}, ip={}", 
                   request.getUsername(), getClientIpAddress(httpRequest));
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 输入验证
            validateLoginRequest(request);
            logger.debug("登录请求验证通过: username={}", request.getUsername());
            
            // 2. 查找用户
            User user = findUserByUsernameOrEmail(request.getUsername());
            logger.debug("用户查找成功: id={}, username={}, email={}, role={}, active={}", 
                        user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getIsActive());
            
            // 3. 验证密码
            validatePassword(request.getPassword(), user);
            logger.debug("密码验证成功: userId={}, username={}", user.getId(), user.getUsername());
            
            // 4. 检查用户状态
            validateUserStatus(user);
            logger.debug("用户状态验证通过: userId={}, username={}", user.getId(), user.getUsername());
            
            // 5. 更新最后登录时间和IP
            updateLastLoginInfo(user, httpRequest);
            
            // 6. 创建用户会话记录
            createUserSession(user, httpRequest);
            
            // 7. 生成JWT Token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            Long expiresIn = jwtUtils.getTokenRemainingTime(token);
            
            long endTime = System.currentTimeMillis();
            logger.info("用户登录成功: username={}, userId={}, 耗时={}ms", 
                       request.getUsername(), user.getId(), (endTime - startTime));
            
            return new TokenResponse(token, expiresIn);
            
        } catch (BusinessException e) {
            long endTime = System.currentTimeMillis();
            logger.error("用户登录失败: username={}, error={}, 耗时={}ms", 
                        request.getUsername(), e.getMessage(), (endTime - startTime));
            throw e;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("用户登录异常: username={}, error={}, 耗时={}ms", 
                        request.getUsername(), e.getMessage(), (endTime - startTime), e);
            throw new BusinessException("登录服务异常，请稍后重试");
        }
    }

    /**
     * 用户登录（包含用户信息）
     * 
     * @param request 登录请求
     * @param httpRequest HTTP请求对象
     * @return 包含token和用户信息的响应
     * @throws BusinessException 登录失败时抛出
     */
    public Map<String, Object> loginWithUserInfo(LoginRequest request, HttpServletRequest httpRequest) {
        logger.info("用户登录请求（含用户信息）开始: username={}, ip={}", 
                   request.getUsername(), getClientIpAddress(httpRequest));
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 输入验证
            validateLoginRequest(request);
            logger.debug("登录请求验证通过: username={}", request.getUsername());
            
            // 2. 查找用户
            User user = findUserByUsernameOrEmail(request.getUsername());
            logger.debug("用户查找成功: id={}, username={}, email={}, role={}, active={}", 
                        user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getIsActive());
            
            // 3. 验证密码
            validatePassword(request.getPassword(), user);
            logger.debug("密码验证成功: userId={}, username={}", user.getId(), user.getUsername());
            
            // 4. 检查用户状态
            validateUserStatus(user);
            logger.debug("用户状态验证通过: userId={}, username={}", user.getId(), user.getUsername());
            
            // 5. 更新最后登录时间和IP
            updateLastLoginInfo(user, httpRequest);
            
            // 6. 创建用户会话记录
            createUserSession(user, httpRequest);
            
            // 7. 生成JWT Token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            Long expiresIn = jwtUtils.getTokenRemainingTime(token);
            
            // 8. 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", convertToUserResponse(user));
            response.put("expires_in", expiresIn);
            response.put("login_time", LocalDateTime.now());
            
            long endTime = System.currentTimeMillis();
            logger.info("用户登录成功（含用户信息）: username={}, userId={}, 耗时={}ms", 
                       request.getUsername(), user.getId(), (endTime - startTime));
            
            return response;
            
        } catch (BusinessException e) {
            long endTime = System.currentTimeMillis();
            logger.error("用户登录失败（含用户信息）: username={}, error={}, 耗时={}ms", 
                        request.getUsername(), e.getMessage(), (endTime - startTime));
            throw e;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("用户登录异常（含用户信息）: username={}, error={}, 耗时={}ms", 
                        request.getUsername(), e.getMessage(), (endTime - startTime), e);
            throw new BusinessException("登录服务异常，请稍后重试");
        }
    }

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @param httpRequest HTTP请求对象
     * @return 用户响应
     * @throws BusinessException 注册失败时抛出
     */
    public UserResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        logger.info("用户注册请求开始: username={}, email={}, ip={}", 
                   request.getUsername(), request.getEmail(), getClientIpAddress(httpRequest));
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 输入验证
            validateRegisterRequest(request);
            logger.debug("注册请求验证通过: username={}, email={}", request.getUsername(), request.getEmail());
            
            // 2. 检查用户名和邮箱是否已存在
            checkUserExistence(request.getUsername(), request.getEmail());
            logger.debug("用户名和邮箱可用性检查通过: username={}, email={}", request.getUsername(), request.getEmail());
            
            // 3. 创建新用户
            User user = createNewUser(request);
            logger.debug("新用户创建成功: id={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());
            
            // 4. 保存用户
            user = userRepository.save(user);
            logger.debug("新用户保存成功: id={}, username={}", user.getId(), user.getUsername());
            
            // 5. 异步创建用户会话记录
            createUserSessionAsync(user, httpRequest);
            
            // 6. 转换为响应对象
            UserResponse response = convertToUserResponse(user);
            
            long endTime = System.currentTimeMillis();
            logger.info("用户注册成功: username={}, userId={}, 耗时={}ms", 
                       request.getUsername(), user.getId(), (endTime - startTime));
            
            return response;
            
        } catch (BusinessException e) {
            long endTime = System.currentTimeMillis();
            logger.error("用户注册失败: username={}, email={}, error={}, 耗时={}ms", 
                        request.getUsername(), request.getEmail(), e.getMessage(), (endTime - startTime));
            throw e;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("用户注册异常: username={}, email={}, error={}, 耗时={}ms", 
                        request.getUsername(), request.getEmail(), e.getMessage(), (endTime - startTime), e);
            throw new BusinessException("注册服务异常，请稍后重试");
        }
    }

    /**
     * 获取当前用户信息
     * 
     * @param user 当前用户
     * @return 用户响应
     */
    public UserResponse getCurrentUserInfo(User user) {
        logger.debug("获取当前用户信息: userId={}, username={}", user.getId(), user.getUsername());
        
        try {
            UserResponse response = convertToUserResponse(user);
            logger.debug("获取当前用户信息成功: userId={}, username={}", user.getId(), user.getUsername());
            return response;
        } catch (Exception e) {
            logger.error("获取当前用户信息失败: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage(), e);
            throw new BusinessException("获取用户信息失败");
        }
    }

    /**
     * 更新用户信息
     * 
     * @param user 当前用户
     * @param updates 更新字段
     * @return 更新后的用户响应
     * @throws BusinessException 更新失败时抛出
     */
    public UserResponse updateUserInfo(User user, Map<String, Object> updates) {
        logger.info("更新用户信息请求: userId={}, username={}, updateFields={}", 
                   user.getId(), user.getUsername(), updates.keySet());
        
        try {
            // 1. 验证更新字段
            validateUpdateFields(updates);
            
            // 2. 应用更新
            applyUserUpdates(user, updates);
            
            // 3. 设置更新时间
            user.setUpdatedAt(LocalDateTime.now());
            
            // 4. 保存更新
            user = userRepository.save(user);
            logger.debug("用户信息更新成功: userId={}, username={}", user.getId(), user.getUsername());
            
            // 5. 转换为响应对象
            UserResponse response = convertToUserResponse(user);
            
            logger.info("用户信息更新完成: userId={}, username={}", user.getId(), user.getUsername());
            
            return response;
            
        } catch (BusinessException e) {
            logger.error("用户信息更新失败: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("用户信息更新异常: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage(), e);
            throw new BusinessException("更新用户信息失败，请稍后重试");
        }
    }

    /**
     * 修改密码
     * 
     * @param user 当前用户
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @throws BusinessException 修改失败时抛出
     */
    public void changePassword(User user, String currentPassword, String newPassword) {
        logger.info("修改密码请求: userId={}, username={}", user.getId(), user.getUsername());
        
        try {
            // 1. 验证当前密码
            validateCurrentPassword(user, currentPassword);
            logger.debug("当前密码验证通过: userId={}, username={}", user.getId(), user.getUsername());
            
            // 2. 验证新密码
            validateNewPassword(newPassword, user.getHashedPassword());
            logger.debug("新密码验证通过: userId={}, username={}", user.getId(), user.getUsername());
            
            // 3. 更新密码
            String newHashedPassword = passwordEncoder.encode(newPassword);
            user.setHashedPassword(newHashedPassword);
            user.setUpdatedAt(LocalDateTime.now());
            
            // 4. 保存更新
            userRepository.save(user);
            logger.debug("密码更新成功: userId={}, username={}", user.getId(), user.getUsername());
            
            // 5. 登出其他设备（可选）
            logoutOtherDevices(user);
            
            logger.info("修改密码成功: userId={}, username={}", user.getId(), user.getUsername());
            
        } catch (BusinessException e) {
            logger.error("修改密码失败: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("修改密码异常: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage(), e);
            throw new BusinessException("修改密码失败，请稍后重试");
        }
    }

    /**
     * 用户登出
     * 
     * @param user 当前用户
     */
    public void logout(User user) {
        logger.info("用户登出请求: userId={}, username={}", user.getId(), user.getUsername());
        
        try {
            userSessionService.logoutAllDevices(user);
            logger.info("用户登出成功: userId={}, username={}", user.getId(), user.getUsername());
        } catch (Exception e) {
            logger.error("用户登出失败: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage(), e);
            throw new BusinessException("登出失败，请稍后重试");
        }
    }

    /**
     * 退出指定设备
     * 
     * @param user 当前用户
     * @param deviceId 设备ID
     */
    public void logoutDevice(User user, Long deviceId) {
        logger.info("退出指定设备请求: userId={}, username={}, deviceId={}", 
                   user.getId(), user.getUsername(), deviceId);
        
        try {
            userSessionService.logoutDevice(user, deviceId);
            logger.info("退出指定设备成功: userId={}, username={}, deviceId={}", 
                       user.getId(), user.getUsername(), deviceId);
        } catch (Exception e) {
            logger.error("退出指定设备失败: userId={}, username={}, deviceId={}, error={}", 
                        user.getId(), user.getUsername(), deviceId, e.getMessage(), e);
            throw new BusinessException("退出设备失败，请稍后重试");
        }
    }

    /**
     * 获取登录设备列表
     * 
     * @param user 当前用户
     * @return 设备列表
     */
    public Map<String, Object> getLoginDevices(User user) {
        logger.debug("获取登录设备列表: userId={}, username={}", user.getId(), user.getUsername());
        
        try {
            Map<String, Object> devices = userSessionService.getLoginDevices(user);
            logger.debug("获取登录设备列表成功: userId={}, username={}, deviceCount={}", 
                        user.getId(), user.getUsername(), 
                        devices.get("devices") != null ? ((java.util.List<?>) devices.get("devices")).size() : 0);
            return devices;
        } catch (Exception e) {
            logger.error("获取登录设备列表失败: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage(), e);
            throw new BusinessException("获取设备列表失败，请稍后重试");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证登录请求
     */
    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new BusinessException("登录请求不能为空");
        }
        
        String username = request.getUsername();
        String password = request.getPassword();
        
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        
        if (username.length() > 100) {
            throw new BusinessException("用户名长度不能超过100个字符");
        }
        
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        
        if (password.length() < 6) {
            throw new BusinessException("密码长度不能少于6个字符");
        }
        
        if (password.length() > 100) {
            throw new BusinessException("密码长度不能超过100个字符");
        }
    }

    /**
     * 验证注册请求
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException("注册请求不能为空");
        }
        
        // 验证用户名
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException("用户名只能包含字母、数字、下划线，长度3-20个字符");
        }
        
        // 验证邮箱
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
        
        // 验证密码
        String password = request.getPassword();
        if (password == null || password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new BusinessException("密码长度不能少于6个字符");
        }
        if (password.length() > 100) {
            throw new BusinessException("密码长度不能超过100个字符");
        }
        
        // 验证全名
        String fullName = request.getFullName();
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (fullName.length() > 50) {
            throw new BusinessException("姓名长度不能超过50个字符");
        }
        
        // 验证角色
        UserRole role = request.getRole();
        if (role == null) {
            throw new BusinessException("用户角色不能为空");
        }
        if (role == UserRole.ADMIN) {
            throw new BusinessException("不允许注册管理员账户");
        }
    }

    /**
     * 根据用户名或邮箱查找用户
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        // 先尝试根据用户名查找
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        
        // 如果没找到，尝试根据邮箱查找
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        if (userOpt.isEmpty()) {
            logger.warn("用户不存在: usernameOrEmail={}", usernameOrEmail);
            throw new BusinessException("用户名或密码错误");
        }
        
        User user = userOpt.get();
        logger.debug("用户查找成功: id={}, username={}, email={}", 
                    user.getId(), user.getUsername(), user.getEmail());
        
        return user;
    }

    /**
     * 验证密码
     */
    private void validatePassword(String inputPassword, User user) {
        String storedPasswordHash = user.getHashedPassword();
        
        if (storedPasswordHash == null || storedPasswordHash.isEmpty()) {
            logger.error("用户密码哈希为空: userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException("用户密码配置错误");
        }
        
        logger.debug("开始验证密码: userId={}, username={}, hashPrefix={}", 
                    user.getId(), user.getUsername(), 
                    storedPasswordHash.length() > 10 ? storedPasswordHash.substring(0, 10) + "..." : storedPasswordHash);
        
        boolean matches = passwordEncoder.matches(inputPassword, storedPasswordHash);
        
        if (!matches) {
            logger.warn("密码验证失败: userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 验证用户状态
     */
    private void validateUserStatus(User user) {
        if (user.getIsActive() == null || !user.getIsActive()) {
            logger.warn("用户账户已被禁用: userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException("用户账户已被禁用，请联系管理员");
        }
        
        // 可以添加更多状态检查，如账户是否过期等
        // 注释掉教师邮箱验证要求，允许未验证的教师登录
        // if (user.getIsVerified() != null && !user.getIsVerified() && user.getRole() == UserRole.TEACHER) {
        //     logger.warn("教师账户未验证: userId={}, username={}", user.getId(), user.getUsername());
        //     throw new BusinessException("教师账户需要先进行邮箱验证");
        // }
    }

    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(User user, HttpServletRequest request) {
        try {
            user.setLastLogin(LocalDateTime.now());
            // 可以添加更多信息，如登录IP、设备信息等
            userRepository.save(user);
            logger.debug("更新最后登录时间成功: userId={}, username={}", user.getId(), user.getUsername());
        } catch (Exception e) {
            logger.error("更新最后登录时间失败: userId={}, username={}, error={}", 
                        user.getId(), user.getUsername(), e.getMessage(), e);
            // 不影响登录流程，只记录错误
        }
    }

    /**
     * 创建用户会话
     */
    private void createUserSession(User user, HttpServletRequest request) {
        try {
            userSessionService.createUserSessionAsync(user, request);
            logger.debug("创建用户会话成功: userId={}, username={}", user.getId(), user.getUsername());
        } catch (Exception e) {
            logger.warn("创建用户会话失败: userId={}, username={}, error={}", 
                       user.getId(), user.getUsername(), e.getMessage());
            // 不影响登录流程
        }
    }

    /**
     * 异步创建用户会话
     */
    private void createUserSessionAsync(User user, HttpServletRequest request) {
        try {
            userSessionService.createUserSessionAsync(user, request);
            logger.debug("异步创建用户会话成功: userId={}, username={}", user.getId(), user.getUsername());
        } catch (Exception e) {
            logger.warn("异步创建用户会话失败: userId={}, username={}, error={}", 
                       user.getId(), user.getUsername(), e.getMessage());
            // 不影响注册流程
        }
    }

    /**
     * 检查用户存在性
     */
    private void checkUserExistence(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            logger.warn("用户名已存在: username={}", username);
            throw new BusinessException("用户名已存在，请选择其他用户名");
        }
        
        if (userRepository.existsByEmail(email)) {
            logger.warn("邮箱已被注册: email={}", email);
            throw new BusinessException("邮箱已被注册，请使用其他邮箱或直接登录");
        }
    }

    /**
     * 创建新用户
     */
    private User createNewUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(true);
        user.setIsVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return user;
    }

    /**
     * 验证更新字段
     */
    private void validateUpdateFields(Map<String, Object> updates) {
        String[] allowedFields = {
            "fullName", "phone", "avatarUrl", "studentId", "major", 
            "grade", "className", "teacherId", "department", "title", "bio"
        };
        
        for (String key : updates.keySet()) {
            boolean isAllowed = false;
            for (String allowedField : allowedFields) {
                if (allowedField.equals(key)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new BusinessException("不允许更新字段: " + key);
            }
        }
        
        // 验证具体字段值
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            validateFieldValue(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 验证字段值
     */
    private void validateFieldValue(String field, Object value) {
        if (value == null) {
            return; // 允许设置为null
        }
        
        String strValue = value.toString();
        
        switch (field) {
            case "fullName":
                if (strValue.length() > 50) {
                    throw new BusinessException("姓名长度不能超过50个字符");
                }
                break;
            case "phone":
                if (strValue.length() > 20) {
                    throw new BusinessException("电话号码长度不能超过20个字符");
                }
                break;
            case "email":
                if (!EMAIL_PATTERN.matcher(strValue).matches()) {
                    throw new BusinessException("邮箱格式不正确");
                }
                break;
            case "bio":
                if (strValue.length() > 500) {
                    throw new BusinessException("个人简介长度不能超过500个字符");
                }
                break;
            default:
                if (strValue.length() > 100) {
                    throw new BusinessException(field + "长度不能超过100个字符");
                }
                break;
        }
    }

    /**
     * 应用用户更新
     */
    private void applyUserUpdates(User user, Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            
            try {
                switch (field) {
                    case "fullName" -> user.setFullName((String) value);
                    case "phone" -> user.setPhone((String) value);
                    case "avatarUrl" -> user.setAvatarUrl((String) value);
                    case "studentId" -> user.setStudentId((String) value);
                    case "major" -> user.setMajor((String) value);
                    case "grade" -> user.setGrade((String) value);
                    case "className" -> user.setClassName((String) value);
                    case "teacherId" -> user.setTeacherId((String) value);
                    case "department" -> user.setDepartment((String) value);
                    case "title" -> user.setTitle((String) value);
                    case "bio" -> user.setBio((String) value);
                    default -> logger.warn("未知的更新字段: {}", field);
                }
            } catch (ClassCastException e) {
                throw new BusinessException("字段 " + field + " 的值类型不正确");
            }
        }
    }

    /**
     * 验证当前密码
     */
    private void validateCurrentPassword(User user, String currentPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new BusinessException("当前密码不能为空");
        }
        
        if (!passwordEncoder.matches(currentPassword, user.getHashedPassword())) {
            logger.warn("当前密码验证失败: userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException("当前密码错误");
        }
    }

    /**
     * 验证新密码
     */
    private void validateNewPassword(String newPassword, String currentHashedPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BusinessException("新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6个字符");
        }
        
        if (newPassword.length() > 100) {
            throw new BusinessException("新密码长度不能超过100个字符");
        }
        
        // 检查新密码是否与当前密码相同
        if (passwordEncoder.matches(newPassword, currentHashedPassword)) {
            throw new BusinessException("新密码不能与当前密码相同");
        }
        
        // 可以添加更多密码复杂度检查
        // if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
        //     throw new BusinessException("密码必须包含大小写字母、数字和特殊字符，且长度至少8位");
        // }
    }

    /**
     * 登出其他设备
     */
    private void logoutOtherDevices(User user) {
        try {
            userSessionService.logoutAllDevices(user);
            logger.debug("登出其他设备成功: userId={}, username={}", user.getId(), user.getUsername());
        } catch (Exception e) {
            logger.warn("登出其他设备失败: userId={}, username={}, error={}", 
                       user.getId(), user.getUsername(), e.getMessage());
            // 不影响密码修改流程
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 转换为用户响应DTO
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        // UserResponse 设计时已经排除了敏感信息（如hashedPassword），所以不需要额外处理
        return response;
    }
}