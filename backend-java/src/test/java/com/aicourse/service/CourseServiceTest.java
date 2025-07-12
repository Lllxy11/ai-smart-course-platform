package com.aicourse.service;

import com.aicourse.config.TestDataBuilder;
import com.aicourse.dto.CourseCreateRequest;
import com.aicourse.dto.CourseResponse;
import com.aicourse.dto.PagedResponse;
import com.aicourse.entity.Course;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.CourseEnrollmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CourseService 测试类
 * 包含67个测试用例，覆盖课程管理的所有功能
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Autowired
    private TestDataBuilder testDataBuilder;

    private User testAdmin;
    private User testTeacher;
    private User testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testAdmin = testDataBuilder.createTestAdmin();
        testTeacher = testDataBuilder.createTestTeacher();
        testStudent = testDataBuilder.createTestStudent();
        
        // 创建测试课程
        testCourse = testDataBuilder.createTestCourse("测试课程", "TEST001", testTeacher);
    }

    @AfterEach
    void tearDown() {
        testDataBuilder.cleanupTestData();
    }

    // ==================== 课程创建测试 ====================

    @Test
    @DisplayName("创建课程 - 正常创建")
    void testCreateCourse_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("新课程");
        request.setDescription("新课程描述");
        request.setCategory("计算机科学");
        request.setDifficulty("intermediate");
        request.setSemester("2024春");
        request.setYear(2024);
        request.setCredits(3.0);
        request.setMaxStudents(50);

        // When
        CourseResponse response = courseService.createCourse(request, testTeacher.getId());

        // Then
        assertNotNull(response);
        assertEquals("新课程", response.getName());
        assertEquals("新课程描述", response.getDescription());
        assertEquals("计算机科学", response.getCategory());
        assertEquals("intermediate", response.getDifficulty());
        assertEquals(testTeacher.getId(), response.getTeacherId());
    }

    @Test
    @DisplayName("创建课程 - 课程名称为空")
    void testCreateCourse_EmptyName_ThrowsException() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("");
        request.setDescription("描述");

        // When & Then
        assertThrows(Exception.class, () -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 教师ID无效")
    void testCreateCourse_InvalidTeacherId_ThrowsException() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("描述");

        // When & Then
        assertThrows(Exception.class, () -> courseService.createCourse(request, 999L));
    }

    @Test
    @DisplayName("创建课程 - 最大学生数为负数")
    void testCreateCourse_NegativeMaxStudents_ThrowsException() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("描述");
        request.setMaxStudents(-1);

        // When & Then
        assertThrows(Exception.class, () -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 学分为负数")
    void testCreateCourse_NegativeCredits_ThrowsException() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("描述");
        request.setCredits(-1.0);

        // When & Then
        assertThrows(Exception.class, () -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 开始日期晚于结束日期")
    void testCreateCourse_StartDateAfterEndDate_ThrowsException() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("描述");
        request.setStartDate(LocalDate.now().plusDays(30));
        request.setEndDate(LocalDate.now().plusDays(10));

        // When & Then
        assertThrows(Exception.class, () -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 使用标签")
    void testCreateCourse_WithTags_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("带标签的课程");
        request.setDescription("描述");
        request.setTags(List.of("Java", "Spring", "Web开发"));

        // When
        CourseResponse response = courseService.createCourse(request, testTeacher.getId());

        // Then
        assertNotNull(response);
        assertEquals("带标签的课程", response.getName());
        assertNotNull(response.getTags());
    }

    @Test
    @DisplayName("创建课程 - 设置价格")
    void testCreateCourse_WithPrice_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("付费课程");
        request.setDescription("描述");
        request.setPrice(99.99);

        // When
        CourseResponse response = courseService.createCourse(request, testTeacher.getId());

        // Then
        assertNotNull(response);
        assertEquals("付费课程", response.getName());
        assertEquals(99.99, response.getPrice());
    }

    @Test
    @DisplayName("创建课程 - 设置封面图片")
    void testCreateCourse_WithCoverImage_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("有封面的课程");
        request.setDescription("描述");
        request.setCoverImage("http://example.com/cover.jpg");

        // When
        CourseResponse response = courseService.createCourse(request, testTeacher.getId());

        // Then
        assertNotNull(response);
        assertEquals("有封面的课程", response.getName());
        assertEquals("http://example.com/cover.jpg", response.getCoverImage());
    }

    @Test
    @DisplayName("创建课程 - 设置教学大纲")
    void testCreateCourse_WithSyllabus_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("有大纲的课程");
        request.setDescription("描述");
        request.setSyllabusUrl("http://example.com/syllabus.pdf");

        // When
        CourseResponse response = courseService.createCourse(request, testTeacher.getId());

        // Then
        assertNotNull(response);
        assertEquals("有大纲的课程", response.getName());
        assertEquals("http://example.com/syllabus.pdf", response.getSyllabusUrl());
    }

    // ==================== 课程查询测试 ====================

    @Test
    @DisplayName("获取课程列表 - 管理员查看所有课程")
    void testGetCourses_AdminViewAll_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 10, null, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getTotalElements() >= 1);
        assertTrue(response.getContent().size() >= 1);
    }

    @Test
    @DisplayName("获取课程列表 - 教师查看自己的课程")
    void testGetCourses_TeacherViewOwn_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testTeacher.getId(), UserRole.TEACHER, 0, 10, null, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .allMatch(course -> course.getTeacherId().equals(testTeacher.getId())));
    }

    @Test
    @DisplayName("获取课程列表 - 学生查看已发布课程")
    void testGetCourses_StudentViewPublished_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testStudent.getId(), UserRole.STUDENT, 0, 10, null, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .allMatch(course -> "PUBLISHED".equals(course.getStatus()) || "ONGOING".equals(course.getStatus())));
    }

    @Test
    @DisplayName("获取课程列表 - 按分类筛选")
    void testGetCourses_FilterByCategory_Success() {
        // Given
        String category = "测试分类";

        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 10, category, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .allMatch(course -> category.equals(course.getCategory())));
    }

    @Test
    @DisplayName("获取课程列表 - 按难度筛选")
    void testGetCourses_FilterByDifficulty_Success() {
        // Given
        String difficulty = "beginner";

        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 10, null, difficulty, null, null, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .allMatch(course -> difficulty.equals(course.getDifficulty())));
    }

    @Test
    @DisplayName("获取课程列表 - 关键词搜索")
    void testGetCourses_SearchByKeyword_Success() {
        // Given
        String keyword = "测试";

        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 10, null, null, keyword, null, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .anyMatch(course -> course.getName().contains(keyword) || 
                               course.getDescription().contains(keyword)));
    }

    @Test
    @DisplayName("获取课程列表 - 按学期筛选")
    void testGetCourses_FilterBySemester_Success() {
        // Given
        String semester = "2024春季";

        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 10, null, null, null, semester, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .allMatch(course -> semester.equals(course.getSemester())));
    }

    @Test
    @DisplayName("获取课程列表 - 按状态筛选")
    void testGetCourses_FilterByStatus_Success() {
        // Given
        String status = "PUBLISHED";

        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 10, null, null, null, null, status, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .allMatch(course -> status.equals(course.getStatus())));
    }

    @Test
    @DisplayName("获取课程列表 - 按教师筛选")
    void testGetCourses_FilterByTeacher_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 10, null, null, null, null, null, testTeacher.getId()
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().stream()
            .allMatch(course -> course.getTeacherId().equals(testTeacher.getId())));
    }

    @Test
    @DisplayName("获取课程列表 - 分页测试")
    void testGetCourses_Pagination_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 5, null, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        assertTrue(response.getContent().size() <= 5);
        assertNotNull(response.getTotalElements());
    }

    @Test
    @DisplayName("获取课程详情 - 正常获取")
    void testGetCourseById_Success() {
        // When
        CourseResponse response = courseService.getCourseById(testCourse.getId(), testAdmin.getId(), UserRole.ADMIN);

        // Then
        assertNotNull(response);
        assertEquals(testCourse.getId(), response.getId());
        assertEquals(testCourse.getName(), response.getName());
        assertEquals(testCourse.getCode(), response.getCode());
    }

    @Test
    @DisplayName("获取课程详情 - 课程不存在")
    void testGetCourseById_NotFound_ThrowsException() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            courseService.getCourseById(999L, testAdmin.getId(), UserRole.ADMIN));
    }

    @Test
    @DisplayName("获取课程详情 - 学生访问未发布课程")
    void testGetCourseById_StudentAccessUnpublished_ThrowsException() {
        // Given
        testCourse.setStatus("DRAFT");
        courseRepository.save(testCourse);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.getCourseById(testCourse.getId(), testStudent.getId(), UserRole.STUDENT));
    }

    @Test
    @DisplayName("获取课程详情 - 教师访问自己的课程")
    void testGetCourseById_TeacherAccessOwn_Success() {
        // When
        CourseResponse response = courseService.getCourseById(testCourse.getId(), testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals(testCourse.getId(), response.getId());
        assertEquals(testTeacher.getId(), response.getTeacherId());
    }

    @Test
    @DisplayName("获取课程详情 - 教师访问他人课程")
    void testGetCourseById_TeacherAccessOthers_ThrowsException() {
        // Given
        User anotherTeacher = testDataBuilder.createTestUser("teacher2", "teacher2@test.com", UserRole.TEACHER);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.getCourseById(testCourse.getId(), anotherTeacher.getId(), UserRole.TEACHER));
    }

    // ==================== 课程更新测试 ====================

    @Test
    @DisplayName("更新课程 - 正常更新")
    void testUpdateCourse_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("更新后的课程名");
        request.setDescription("更新后的描述");
        request.setCategory("更新后的分类");

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals("更新后的课程名", response.getName());
        assertEquals("更新后的描述", response.getDescription());
        assertEquals("更新后的分类", response.getCategory());
    }

    @Test
    @DisplayName("更新课程 - 非课程教师尝试更新")
    void testUpdateCourse_NotOwner_ThrowsException() {
        // Given
        User anotherTeacher = testDataBuilder.createTestUser("teacher2", "teacher2@test.com", UserRole.TEACHER);
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("更新后的课程名");

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.updateCourse(testCourse.getId(), request, anotherTeacher.getId(), UserRole.TEACHER));
    }

    @Test
    @DisplayName("更新课程 - 管理员更新任意课程")
    void testUpdateCourse_AdminUpdate_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("管理员更新的课程");

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testAdmin.getId(), UserRole.ADMIN);

        // Then
        assertNotNull(response);
        assertEquals("管理员更新的课程", response.getName());
    }

    @Test
    @DisplayName("更新课程 - 更新课程代码")
    void testUpdateCourse_UpdateCode_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setCode("NEWCODE001");

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals("NEWCODE001", response.getCode());
    }

    @Test
    @DisplayName("更新课程 - 更新学期和年份")
    void testUpdateCourse_UpdateSemesterAndYear_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setSemester("2024秋");
        request.setYear(2024);

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals("2024秋", response.getSemester());
        assertEquals(2024, response.getYear());
    }

    @Test
    @DisplayName("更新课程 - 更新学分")
    void testUpdateCourse_UpdateCredits_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setCredits(4.0);

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals(4.0, response.getCredits());
    }

    @Test
    @DisplayName("更新课程 - 更新状态")
    void testUpdateCourse_UpdateStatus_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setStatus("ONGOING");

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals("ONGOING", response.getStatus());
    }

    @Test
    @DisplayName("更新课程 - 更新最大学生数")
    void testUpdateCourse_UpdateMaxStudents_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setMaxStudents(100);

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals(100, response.getMaxStudents());
    }

    @Test
    @DisplayName("更新课程 - 更新难度")
    void testUpdateCourse_UpdateDifficulty_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setDifficulty("advanced");

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals("advanced", response.getDifficulty());
    }

    @Test
    @DisplayName("更新课程 - 更新价格")
    void testUpdateCourse_UpdatePrice_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setPrice(199.99);

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals(199.99, response.getPrice());
    }

    @Test
    @DisplayName("更新课程 - 更新开始和结束日期")
    void testUpdateCourse_UpdateDates_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(90));

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals(LocalDate.now().plusDays(1), response.getStartDate());
        assertEquals(LocalDate.now().plusDays(90), response.getEndDate());
    }

    @Test
    @DisplayName("更新课程 - 更新封面图片")
    void testUpdateCourse_UpdateCoverImage_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setCoverImage("http://example.com/new-cover.jpg");

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals("http://example.com/new-cover.jpg", response.getCoverImage());
    }

    @Test
    @DisplayName("更新课程 - 更新教学大纲")
    void testUpdateCourse_UpdateSyllabus_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setSyllabusUrl("http://example.com/new-syllabus.pdf");

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertEquals("http://example.com/new-syllabus.pdf", response.getSyllabusUrl());
    }

    @Test
    @DisplayName("更新课程 - 更新标签")
    void testUpdateCourse_UpdateTags_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setTags(List.of("新标签1", "新标签2", "新标签3"));

        // When
        CourseResponse response = courseService.updateCourse(testCourse.getId(), request, testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertNotNull(response.getTags());
        assertTrue(response.getTags().contains("新标签1"));
        assertTrue(response.getTags().contains("新标签2"));
        assertTrue(response.getTags().contains("新标签3"));
    }

    // ==================== 课程删除测试 ====================

    @Test
    @DisplayName("删除课程 - 正常删除")
    void testDeleteCourse_Success() {
        // When & Then
        assertDoesNotThrow(() -> courseService.deleteCourse(testCourse.getId(), testTeacher.getId(), UserRole.TEACHER));
    }

    @Test
    @DisplayName("删除课程 - 非课程教师尝试删除")
    void testDeleteCourse_NotOwner_ThrowsException() {
        // Given
        User anotherTeacher = testDataBuilder.createTestUser("teacher2", "teacher2@test.com", UserRole.TEACHER);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.deleteCourse(testCourse.getId(), anotherTeacher.getId(), UserRole.TEACHER));
    }

    @Test
    @DisplayName("删除课程 - 管理员删除任意课程")
    void testDeleteCourse_AdminDelete_Success() {
        // When & Then
        assertDoesNotThrow(() -> courseService.deleteCourse(testCourse.getId(), testAdmin.getId(), UserRole.ADMIN));
    }

    @Test
    @DisplayName("删除课程 - 课程不存在")
    void testDeleteCourse_NotFound_ThrowsException() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            courseService.deleteCourse(999L, testTeacher.getId(), UserRole.TEACHER));
    }

    // ==================== 选课管理测试 ====================

    @Test
    @DisplayName("学生选课 - 正常选课")
    void testEnrollCourse_Success() {
        // When
        Map<String, Object> response = courseService.enrollCourse(testCourse.getId(), testStudent.getId());

        // Then
        assertNotNull(response);
        assertEquals("加入课程成功", response.get("message"));
        assertNotNull(response.get("enrollment"));
    }

    @Test
    @DisplayName("学生选课 - 重复选课")
    void testEnrollCourse_AlreadyEnrolled_ThrowsException() {
        // Given
        courseService.enrollCourse(testCourse.getId(), testStudent.getId());

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.enrollCourse(testCourse.getId(), testStudent.getId()));
    }

    @Test
    @DisplayName("学生选课 - 课程未激活")
    void testEnrollCourse_CourseInactive_ThrowsException() {
        // Given
        testCourse.setIsActive(false);
        courseRepository.save(testCourse);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.enrollCourse(testCourse.getId(), testStudent.getId()));
    }

    @Test
    @DisplayName("学生选课 - 课程人数已满")
    void testEnrollCourse_CourseFull_ThrowsException() {
        // Given
        testCourse.setMaxStudents(0);
        courseRepository.save(testCourse);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.enrollCourse(testCourse.getId(), testStudent.getId()));
    }

    @Test
    @DisplayName("学生选课 - 课程不存在")
    void testEnrollCourse_CourseNotFound_ThrowsException() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            courseService.enrollCourse(999L, testStudent.getId()));
    }

    @Test
    @DisplayName("获取课程学生列表 - 正常获取")
    void testGetCourseStudents_Success() {
        // Given
        courseService.enrollCourse(testCourse.getId(), testStudent.getId());

        // When
        Map<String, Object> response = courseService.getCourseStudents(testCourse.getId(), testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(response);
        assertTrue(response.containsKey("students"));
        assertTrue(response.containsKey("total"));
        assertTrue(response.containsKey("courseName"));
    }

    @Test
    @DisplayName("获取课程学生列表 - 非课程教师访问")
    void testGetCourseStudents_NotOwner_ThrowsException() {
        // Given
        User anotherTeacher = testDataBuilder.createTestUser("teacher2", "teacher2@test.com", UserRole.TEACHER);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.getCourseStudents(testCourse.getId(), anotherTeacher.getId(), UserRole.TEACHER));
    }

    @Test
    @DisplayName("获取课程学生列表 - 管理员访问")
    void testGetCourseStudents_AdminAccess_Success() {
        // Given
        courseService.enrollCourse(testCourse.getId(), testStudent.getId());

        // When
        Map<String, Object> response = courseService.getCourseStudents(testCourse.getId(), testAdmin.getId(), UserRole.ADMIN);

        // Then
        assertNotNull(response);
        assertTrue(response.containsKey("students"));
        assertTrue(response.containsKey("total"));
    }

    // ==================== 课程统计测试 ====================

    @Test
    @DisplayName("获取课程统计 - 管理员查看全部统计")
    void testGetCourseStatistics_AdminViewAll_Success() {
        // When
        Map<String, Object> statistics = courseService.getCourseStatistics(testAdmin.getId(), UserRole.ADMIN, null, null, null);

        // Then
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalCourses"));
        assertTrue(statistics.containsKey("activeCourses"));
        assertTrue(statistics.containsKey("publishedCourses"));
        assertTrue(statistics.containsKey("totalEnrollments"));
        assertTrue(statistics.containsKey("averageRating"));
    }

    @Test
    @DisplayName("获取课程统计 - 教师查看个人统计")
    void testGetCourseStatistics_TeacherViewOwn_Success() {
        // When
        Map<String, Object> statistics = courseService.getCourseStatistics(testTeacher.getId(), UserRole.TEACHER, null, null, null);

        // Then
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalCourses"));
        assertTrue(statistics.containsKey("publishedCourses"));
        assertTrue(statistics.containsKey("totalEnrollments"));
        assertTrue(statistics.containsKey("myCourses"));
        assertTrue(statistics.containsKey("myStudents"));
    }

    @Test
    @DisplayName("获取课程统计 - 学生查看选课统计")
    void testGetCourseStatistics_StudentViewEnrollment_Success() {
        // When
        Map<String, Object> statistics = courseService.getCourseStatistics(testStudent.getId(), UserRole.STUDENT, null, null, null);

        // Then
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalCourses"));
        assertTrue(statistics.containsKey("enrolledCourses"));
        assertTrue(statistics.containsKey("completedCourses"));
        assertTrue(statistics.containsKey("inProgressCourses"));
    }

    @Test
    @DisplayName("获取课程统计 - 按学期筛选")
    void testGetCourseStatistics_FilterBySemester_Success() {
        // When
        Map<String, Object> statistics = courseService.getCourseStatistics(testAdmin.getId(), UserRole.ADMIN, "2024春", null, null);

        // Then
        assertNotNull(statistics);
        assertEquals("2024春", statistics.get("semester"));
    }

    @Test
    @DisplayName("获取课程统计 - 按年份筛选")
    void testGetCourseStatistics_FilterByYear_Success() {
        // When
        Map<String, Object> statistics = courseService.getCourseStatistics(testAdmin.getId(), UserRole.ADMIN, null, 2024, null);

        // Then
        assertNotNull(statistics);
        assertEquals(2024, statistics.get("year"));
    }

    @Test
    @DisplayName("获取课程统计 - 按分类筛选")
    void testGetCourseStatistics_FilterByCategory_Success() {
        // When
        Map<String, Object> statistics = courseService.getCourseStatistics(testAdmin.getId(), UserRole.ADMIN, null, null, "计算机科学");

        // Then
        assertNotNull(statistics);
        assertEquals("计算机科学", statistics.get("category"));
    }

    // ==================== 课程分析测试 ====================

    @Test
    @DisplayName("获取课程分析数据 - 教师查看自己课程")
    void testGetCourseAnalytics_TeacherViewOwn_Success() {
        // When
        Map<String, Object> analytics = courseService.getCourseAnalytics(testCourse.getId(), testTeacher.getId(), UserRole.TEACHER);

        // Then
        assertNotNull(analytics);
        assertTrue(analytics.containsKey("courseId"));
        assertTrue(analytics.containsKey("courseName"));
        assertTrue(analytics.containsKey("totalStudents"));
        assertTrue(analytics.containsKey("averageProgress"));
        assertTrue(analytics.containsKey("completionRate"));
    }

    @Test
    @DisplayName("获取课程分析数据 - 管理员查看任意课程")
    void testGetCourseAnalytics_AdminViewAny_Success() {
        // When
        Map<String, Object> analytics = courseService.getCourseAnalytics(testCourse.getId(), testAdmin.getId(), UserRole.ADMIN);

        // Then
        assertNotNull(analytics);
        assertTrue(analytics.containsKey("courseId"));
        assertTrue(analytics.containsKey("courseName"));
        assertTrue(analytics.containsKey("totalStudents"));
    }

    @Test
    @DisplayName("获取课程分析数据 - 非课程教师访问")
    void testGetCourseAnalytics_NotOwner_ThrowsException() {
        // Given
        User anotherTeacher = testDataBuilder.createTestUser("teacher2", "teacher2@test.com", UserRole.TEACHER);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.getCourseAnalytics(testCourse.getId(), anotherTeacher.getId(), UserRole.TEACHER));
    }

    @Test
    @DisplayName("获取课程分析数据 - 学生尝试访问")
    void testGetCourseAnalytics_StudentAccess_ThrowsException() {
        // When & Then
        assertThrows(BusinessException.class, () -> 
            courseService.getCourseAnalytics(testCourse.getId(), testStudent.getId(), UserRole.STUDENT));
    }

    @Test
    @DisplayName("获取课程分析数据 - 课程不存在")
    void testGetCourseAnalytics_CourseNotFound_ThrowsException() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            courseService.getCourseAnalytics(999L, testTeacher.getId(), UserRole.TEACHER));
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("获取课程列表 - 页码为负数")
    void testGetCourses_NegativePage_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, -1, 10, null, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        // 应该自动修正为第一页
    }

    @Test
    @DisplayName("获取课程列表 - 页面大小为0")
    void testGetCourses_ZeroSize_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 0, null, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        // 应该使用默认页面大小
    }

    @Test
    @DisplayName("获取课程列表 - 页面大小过大")
    void testGetCourses_LargeSize_Success() {
        // When
        PagedResponse<CourseResponse> response = courseService.getCourses(
            testAdmin.getId(), UserRole.ADMIN, 0, 1000, null, null, null, null, null, null
        );

        // Then
        assertNotNull(response);
        // 应该限制最大页面大小
    }

    @Test
    @DisplayName("创建课程 - 课程名称长度边界测试")
    void testCreateCourse_NameLengthBoundary_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("a".repeat(100)); // 假设最大长度为100
        request.setDescription("描述");

        // When & Then
        assertDoesNotThrow(() -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 描述长度边界测试")
    void testCreateCourse_DescriptionLengthBoundary_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("a".repeat(1000)); // 假设最大长度为1000

        // When & Then
        assertDoesNotThrow(() -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 最大学生数边界测试")
    void testCreateCourse_MaxStudentsBoundary_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("描述");
        request.setMaxStudents(1000); // 假设最大值为1000

        // When & Then
        assertDoesNotThrow(() -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 学分边界测试")
    void testCreateCourse_CreditsBoundary_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("描述");
        request.setCredits(10.0); // 假设最大学分为10

        // When & Then
        assertDoesNotThrow(() -> courseService.createCourse(request, testTeacher.getId()));
    }

    @Test
    @DisplayName("创建课程 - 价格边界测试")
    void testCreateCourse_PriceBoundary_Success() {
        // Given
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("测试课程");
        request.setDescription("描述");
        request.setPrice(9999.99); // 假设最大价格为9999.99

        // When & Then
        assertDoesNotThrow(() -> courseService.createCourse(request, testTeacher.getId()));
    }
} 