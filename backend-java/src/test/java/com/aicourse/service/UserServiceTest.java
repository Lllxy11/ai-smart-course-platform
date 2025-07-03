package com.aicourse.service;

import com.aicourse.config.TestDataBuilder;
import com.aicourse.dto.PagedResponse;
import com.aicourse.dto.UserCreateRequest;
import com.aicourse.dto.UserResponse;
import com.aicourse.dto.UserUpdateRequest;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService 测试类
 * 测试用户管理相关的业务逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataBuilder testDataBuilder;

    @PersistenceContext
    private EntityManager entityManager;

    private User testAdmin;
    private User testTeacher;
    private User testStudent;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testAdmin = testDataBuilder.createTestAdmin();
        testTeacher = testDataBuilder.createTestTeacher();
        testStudent = testDataBuilder.createTestStudent();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        testDataBuilder.cleanupTestData();
    }

    @Test
    @DisplayName("获取用户列表 - 正常情况")
    void testGetUsers_Success() {
        // Given
        int page = 0;
        int size = 10;

        // When
        PagedResponse<UserResponse> result = userService.getUsers(page, size, null, null, testAdmin.getId(), testAdmin.getRole());

        // Then
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 3);
        assertTrue(result.getContent().size() >= 3);
    }

    @Test
    @DisplayName("获取用户列表 - 按角色筛选")
    void testGetUsers_FilterByRole() {
        // Given
        int page = 0;
        int size = 10;

        // When
        PagedResponse<UserResponse> result = userService.getUsers(page, size, UserRole.STUDENT, null, testAdmin.getId(), testAdmin.getRole());

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().stream()
                .allMatch(user -> UserRole.STUDENT.equals(user.getRole())));
    }

    @Test
    @DisplayName("获取用户列表 - 按状态筛选")
    void testGetUsers_FilterByStatus() {
        // Given
        int page = 0;
        int size = 10;

        // When
        PagedResponse<UserResponse> result = userService.getUsers(page, size, null, true, testAdmin.getId(), testAdmin.getRole());

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().stream()
                .allMatch(user -> user.getIsActive()));
    }

    @Test
    @DisplayName("根据ID获取用户 - 正常情况")
    void testGetUserById_Success() {
        // When
        UserResponse result = userService.getUserById(testStudent.getId(), testAdmin.getId(), testAdmin.getRole());

        // Then
        assertNotNull(result);
        assertEquals(testStudent.getId(), result.getId());
        assertEquals(testStudent.getUsername(), result.getUsername());
        assertEquals(testStudent.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("根据ID获取用户 - 用户不存在")
    void testGetUserById_NotFound() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, 
            () -> userService.getUserById(999L, testAdmin.getId(), testAdmin.getRole()));
    }

    @Test
    @DisplayName("创建用户 - 正常情况")
    void testCreateUser_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setFullName("新用户");
        request.setPassword("password123");
        request.setRole(UserRole.STUDENT);

        // When
        UserResponse result = userService.createUser(request, testAdmin.getId(), testAdmin.getRole());

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@test.com", result.getEmail());
        assertEquals("新用户", result.getFullName());
        assertEquals(UserRole.STUDENT, result.getRole());
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("创建用户 - 用户名重复")
    void testCreateUser_DuplicateUsername() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(testStudent.getUsername());
        request.setEmail("different@test.com");
        request.setFullName("不同用户");
        request.setPassword("password123");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, 
            () -> userService.createUser(request, testAdmin.getId(), testAdmin.getRole()));
    }

    @Test
    @DisplayName("创建用户 - 邮箱重复")
    void testCreateUser_DuplicateEmail() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("differentuser");
        request.setEmail(testStudent.getEmail());
        request.setFullName("不同用户");
        request.setPassword("password123");
        request.setRole(UserRole.STUDENT);

        // When & Then
        assertThrows(BusinessException.class, 
            () -> userService.createUser(request, testAdmin.getId(), testAdmin.getRole()));
    }

    @Test
    @DisplayName("更新用户 - 正常情况")
    void testUpdateUser_Success() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("更新后的名称");
        request.setPhone("13800138000");

        // When
        UserResponse result = userService.updateUser(testStudent.getId(), request, testAdmin.getId(), testAdmin.getRole());

        // Then
        assertNotNull(result);
        assertEquals("更新后的名称", result.getFullName());
        assertEquals("13800138000", result.getPhone());
    }

    @Test
    @DisplayName("更新用户 - 用户名冲突")
    void testUpdateUser_UsernameConflict() {
        // Given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername(testTeacher.getUsername());

        // When & Then
        assertThrows(BusinessException.class, 
            () -> userService.updateUser(testStudent.getId(), request, testAdmin.getId(), testAdmin.getRole()));
    }

    @Test
    @DisplayName("删除用户 - 正常情况")
    void testDeleteUser_Success() {
        // Given
        User userToDelete = testDataBuilder.createTestUser("todelete", "delete@test.com", UserRole.STUDENT);

        // When
        userService.deleteUser(userToDelete.getId(), testAdmin.getId(), testAdmin.getRole());

        // Then
        assertFalse(userRepository.findById(userToDelete.getId()).isPresent());
    }

    @Test
    @DisplayName("删除用户 - 删除自己")
    void testDeleteUser_DeleteSelf() {
        // When & Then
        assertThrows(BusinessException.class, 
            () -> userService.deleteUser(testAdmin.getId(), testAdmin.getId(), testAdmin.getRole()));
    }

    @Test
    @DisplayName("激活用户 - 正常情况")
    void testActivateUser_Success() {
        // Given
        User inactiveUser = testDataBuilder.createTestUser("inactive", "inactive@test.com", UserRole.STUDENT);
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);

        // When
        userService.activateUser(inactiveUser.getId(), testAdmin.getId(), testAdmin.getRole());

        // Then
        User updatedUser = userRepository.findById(inactiveUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(updatedUser.getIsActive());
    }

    @Test
    @DisplayName("停用用户 - 正常情况")
    void testDeactivateUser_Success() {
        // When
        userService.deactivateUser(testStudent.getId(), testAdmin.getId(), testAdmin.getRole());

        // Then
        User updatedUser = userRepository.findById(testStudent.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertFalse(updatedUser.getIsActive());
    }

    @Test
    @DisplayName("批量激活用户 - 正常情况")
    void testBatchActivateUsers_Success() {
        // Given
        List<User> inactiveUsers = testDataBuilder.createTestUsers(3, UserRole.STUDENT);
        inactiveUsers.forEach(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });
        // 确保这些用户都不是当前操作的管理员  
        List<Long> userIds = inactiveUsers.stream()
                .map(User::getId)
                .filter(id -> !id.equals(testAdmin.getId()))
                .toList();

        // When
        userService.batchActivateUsers(userIds, testAdmin.getId(), testAdmin.getRole());
        
        // 刷新EntityManager以确保看到最新的数据库状态
        entityManager.flush();
        entityManager.clear();

        // Then
        userIds.forEach(id -> {
            User user = userRepository.findById(id).orElse(null);
            assertNotNull(user);
            assertTrue(user.getIsActive());
        });
    }

    @Test
    @DisplayName("批量停用用户 - 正常情况")
    void testBatchDeactivateUsers_Success() {
        // Given
        List<User> activeUsers = testDataBuilder.createTestUsers(3, UserRole.STUDENT);
        // 确保这些用户都不是当前操作的管理员
        List<Long> userIds = activeUsers.stream()
                .map(User::getId)
                .filter(id -> !id.equals(testAdmin.getId()))
                .toList();

        // When
        userService.batchDeactivateUsers(userIds, testAdmin.getId(), testAdmin.getRole());
        
        // 刷新EntityManager以确保看到最新的数据库状态
        entityManager.flush();
        entityManager.clear();

        // Then
        userIds.forEach(id -> {
            User user = userRepository.findById(id).orElse(null);
            assertNotNull(user);
            assertFalse(user.getIsActive());
        });
    }

    @Test
    @DisplayName("批量删除用户 - 正常情况")
    void testBatchDeleteUsers_Success() {
        // Given
        List<User> usersToDelete = testDataBuilder.createTestUsers(3, UserRole.STUDENT);
        List<Long> userIds = usersToDelete.stream().map(User::getId).toList();

        // When
        userService.batchDeleteUsers(userIds, testAdmin.getId(), testAdmin.getRole());

        // Then
        userIds.forEach(id -> {
            assertFalse(userRepository.findById(id).isPresent());
        });
    }
} 