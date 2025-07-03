package com.aicourse.config;

import com.aicourse.entity.*;
import com.aicourse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试数据构建器
 * 用于在测试中快速创建测试数据
 */
@Component
public class TestDataBuilder {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 创建测试用户
     */
    public User createTestUser(String username, String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setHashedPassword(passwordEncoder.encode("password123"));
        user.setFullName("测试用户_" + username);
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 创建测试管理员
     */
    public User createTestAdmin() {
        return createTestUser("testadmin", "admin@test.com", UserRole.ADMIN);
    }

    /**
     * 创建测试教师
     */
    public User createTestTeacher() {
        return createTestUser("testteacher", "teacher@test.com", UserRole.TEACHER);
    }

    /**
     * 创建测试学生
     */
    public User createTestStudent() {
        return createTestUser("teststudent", "student@test.com", UserRole.STUDENT);
    }

    /**
     * 创建测试课程
     */
    public Course createTestCourse(String name, String code, User teacher) {
        Course course = new Course();
        course.setName(name);
        course.setCode(code);
        course.setDescription("测试课程描述");
        course.setCategory("测试分类");
        course.setDifficulty("beginner");
        course.setMaxStudents(100);
        course.setTeacherId(teacher.getId());
        course.setIsActive(true);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        course.setSemester("2024春季");
        course.setYear(2024);
        return courseRepository.save(course);
    }

    /**
     * 创建测试任务
     */
    public Task createTestTask(String title, Course course, User teacher) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("测试任务描述");
        task.setDueDate(LocalDateTime.now().plusDays(7));
        task.setCourseId(course.getId());
        task.setCreatorId(teacher.getId());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setStatus("active");
        task.setIsPublished(true);
        return taskRepository.save(task);
    }

    /**
     * 创建测试通知
     */
    public Notification createTestNotification(String title, User targetUser) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent("测试通知内容");
        notification.setCategory("system");
        notification.setUserId(targetUser.getId());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }

    /**
     * 批量创建测试用户
     */
    public List<User> createTestUsers(int count, UserRole role) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(createTestUser("testuser" + i, "user" + i + "@test.com", role));
        }
        return users;
    }

    /**
     * 清理所有测试数据
     */
    public void cleanupTestData() {
        notificationRepository.deleteAll();
        taskRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }
} 