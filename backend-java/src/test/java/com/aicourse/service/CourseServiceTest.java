package com.aicourse.service;

import com.aicourse.dto.CourseCreateRequest;
import com.aicourse.dto.CourseResponse;
import com.aicourse.dto.PagedResponse;
import com.aicourse.entity.Course;
import com.aicourse.entity.CourseEnrollment;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseEnrollmentRepository;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.TaskRepository;
import com.aicourse.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Unit Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseEnrollmentRepository courseEnrollmentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CourseService courseService;

    private User admin, teacher, student, otherTeacher;
    private Course course, unpublishedCourse;
    private CourseEnrollment enrollment;
    private CourseCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        admin = createUser(1L, "admin", "Admin User", UserRole.ADMIN);
        teacher = createUser(2L, "teacher", "Teacher User", UserRole.TEACHER);
        student = createUser(3L, "student", "Student User", UserRole.STUDENT);
        otherTeacher = createUser(4L, "other_teacher", "Other Teacher", UserRole.TEACHER);

        course = new Course();
        course.setId(1L);
        course.setName("Test Course");
        course.setTeacherId(teacher.getId());
        course.setIsActive(true);
        course.setStatus("PUBLISHED");
        course.setMaxStudents(50);
        course.setTags("[\"Java\", \"Spring\"]");

        unpublishedCourse = new Course();
        unpublishedCourse.setId(2L);
        unpublishedCourse.setName("Unpublished Course");
        unpublishedCourse.setTeacherId(teacher.getId());
        unpublishedCourse.setIsActive(true);
        unpublishedCourse.setStatus("DRAFT");

        enrollment = new CourseEnrollment();
        enrollment.setId(1L);
        enrollment.setCourseId(course.getId());
        enrollment.setStudentId(student.getId());
        enrollment.setIsActive(true);
        enrollment.setProgress(50.0);

        createRequest = new CourseCreateRequest();
        createRequest.setName("New Course");
        createRequest.setDescription("A new course description.");
        createRequest.setCategory("Programming");
        createRequest.setTags(List.of("NewTag"));
    }

    private User createUser(Long id, String username, String fullName, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);
        return user;
    }

    // =================== getCourses Tests ===================

    @Test
    @DisplayName("getCourses - For Admin with filters")
    void getCourses_forAdminWithFilters() {
        Page<Course> coursePage = new PageImpl<>(List.of(course));
        when(courseRepository.findCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(coursePage);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        PagedResponse<CourseResponse> response = courseService.getCourses(admin.getId(), UserRole.ADMIN, 0, 10, "cat", "diff", "key", "sem", "stat", teacher.getId());

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(courseRepository, times(1)).findCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }
    
    @Test
    @DisplayName("getCourses - For Admin without filters")
    void getCourses_forAdminWithoutFilters() {
        Page<Course> coursePage = new PageImpl<>(List.of(course));
        when(courseRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(coursePage);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        PagedResponse<CourseResponse> response = courseService.getCourses(admin.getId(), UserRole.ADMIN, 0, 10, "", "", "", "", "", null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(courseRepository, times(1)).findByIsActiveTrue(any(Pageable.class));
    }


    @Test
    @DisplayName("getCourses - For Teacher")
    void getCourses_forTeacher() {
        Page<Course> coursePage = new PageImpl<>(List.of(course));
        when(courseRepository.findCoursesWithFilters(eq(teacher.getId()), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(coursePage);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        PagedResponse<CourseResponse> response = courseService.getCourses(teacher.getId(), UserRole.TEACHER, 0, 10, null, null, null, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    @DisplayName("getCourses - For Student with filtering")
    void getCourses_forStudent() {
        // Student should only see published courses
        Course ongoingCourse = new Course();
        ongoingCourse.setId(3L);
        ongoingCourse.setStatus("ONGOING");
        ongoingCourse.setIsActive(true);
        ongoingCourse.setTeacherId(teacher.getId());

        List<Course> allCourses = List.of(course, unpublishedCourse, ongoingCourse);
        Page<Course> coursePage = new PageImpl<>(allCourses);
        when(courseRepository.findCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(coursePage);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));

        PagedResponse<CourseResponse> response = courseService.getCourses(student.getId(), UserRole.STUDENT, 0, 10, null, null, null, null, null, null);

        assertNotNull(response);
        assertEquals(2, response.getContent().size()); // Should be 2 (PUBLISHED and ONGOING)
        assertTrue(response.getContent().stream().allMatch(c -> c.getStatus().equals("PUBLISHED") || c.getStatus().equals("ONGOING")));
    }
    
    @Test
    @DisplayName("getCourses - For Student, empty page")
    void getCourses_forStudentEmptyPage() {
        List<Course> filteredCourses = new ArrayList<>();
        Page<Course> coursePage = new PageImpl<>(filteredCourses);
        when(courseRepository.findCoursesWithFilters(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(coursePage);
        
        PagedResponse<CourseResponse> response = courseService.getCourses(student.getId(), UserRole.STUDENT, 1, 10, null, null, null, null, null, null);
        
        assertTrue(response.getContent().isEmpty());
    }

    // =================== getCourseStatistics Tests ===================
    @Test
    @DisplayName("getCourseStatistics - For Admin")
    void getCourseStatistics_forAdmin() {
        when(courseRepository.count()).thenReturn(10L);
        when(courseRepository.findByIsActiveTrue()).thenReturn(List.of(new Course(), new Course()));
        when(courseRepository.findAll()).thenReturn(List.of(course)); // 1 published
        when(courseEnrollmentRepository.findAll()).thenReturn(List.of(enrollment));

        Map<String, Object> stats = courseService.getCourseStatistics(admin.getId(), UserRole.ADMIN, "2024", 2024, "CS");
        assertEquals(10L, stats.get("totalCourses"));
        assertEquals(2L, stats.get("activeCourses"));
        assertEquals(1L, stats.get("publishedCourses"));
        assertEquals(1L, stats.get("totalEnrollments"));
    }

    @Test
    @DisplayName("getCourseStatistics - For Teacher")
    void getCourseStatistics_forTeacher() {
        when(courseRepository.countByTeacherId(teacher.getId())).thenReturn(5L);
        when(courseRepository.findByTeacherId(teacher.getId())).thenReturn(List.of(course));
        when(courseEnrollmentRepository.countByCourseIdAndIsActiveTrue(course.getId())).thenReturn(20L);

        Map<String, Object> stats = courseService.getCourseStatistics(teacher.getId(), UserRole.TEACHER, "2024", 2024, "CS");
        assertEquals(5L, stats.get("totalCourses"));
        assertEquals(1L, stats.get("publishedCourses"));
        assertEquals(20L, stats.get("totalEnrollments"));
    }

    @Test
    @DisplayName("getCourseStatistics - For Student")
    void getCourseStatistics_forStudent() {
        when(courseEnrollmentRepository.countByStudentIdAndIsActiveTrue(student.getId())).thenReturn(8L);
        when(courseEnrollmentRepository.findCompletedCoursesByStudentId(student.getId())).thenReturn(List.of(new CourseEnrollment(), new CourseEnrollment()));
        when(courseRepository.findEnrolledCoursesByStudentId(student.getId())).thenReturn(List.of(course));

        Map<String, Object> stats = courseService.getCourseStatistics(student.getId(), UserRole.STUDENT, "2024", 2024, "CS");
        assertEquals(8L, stats.get("enrolledCourses"));
        assertEquals(2L, stats.get("completedCourses"));
        assertEquals(1L, stats.get("publishedCourses"));
    }

    // =================== getCourseById Tests ===================
    @Test
    @DisplayName("getCourseById - Success")
    void getCourseById_success() throws JsonProcessingException {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of("Java", "Spring"));

        CourseResponse response = courseService.getCourseById(1L, admin.getId(), UserRole.ADMIN);
        assertNotNull(response);
        assertEquals("Test Course", response.getName());
    }

    @Test
    @DisplayName("getCourseById - Course Not Found")
    void getCourseById_notFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(99L, admin.getId(), UserRole.ADMIN));
    }
    
    // =================== checkCourseAccess Tests (via getCourseById) ===================
    @Test
    @DisplayName("checkCourseAccess - Teacher denied for other's course")
    void checkCourseAccess_teacherDenied() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        assertThrows(BusinessException.class, () -> courseService.getCourseById(1L, otherTeacher.getId(), UserRole.TEACHER));
    }

    @Test
    @DisplayName("checkCourseAccess - Student denied for unpublished course")
    void checkCourseAccess_studentDeniedUnpublished() {
        when(courseRepository.findById(2L)).thenReturn(Optional.of(unpublishedCourse));
        when(courseEnrollmentRepository.existsByStudentIdAndCourseIdAndIsActiveTrue(student.getId(), 2L)).thenReturn(false);
        assertThrows(BusinessException.class, () -> courseService.getCourseById(2L, student.getId(), UserRole.STUDENT));
    }

    @Test
    @DisplayName("checkCourseAccess - Student allowed for enrolled course")
    void checkCourseAccess_studentAllowedEnrolled() {
        when(courseRepository.findById(2L)).thenReturn(Optional.of(unpublishedCourse));
        when(courseEnrollmentRepository.existsByStudentIdAndCourseIdAndIsActiveTrue(student.getId(), 2L)).thenReturn(true);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        assertDoesNotThrow(() -> courseService.getCourseById(2L, student.getId(), UserRole.STUDENT));
    }

    // =================== createCourse Tests ===================
    @Test
    @DisplayName("createCourse - Success")
    void createCourse_success() throws JsonProcessingException {
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"NewTag\"]");

        CourseResponse response = courseService.createCourse(createRequest, teacher.getId());

        assertNotNull(response);
        assertEquals("New Course", response.getName());
        assertTrue(response.getCode().startsWith("COURSE-"));
    }
    
    @Test
    @DisplayName("createCourse - With Null Optional Fields")
    void createCourse_withNullOptionalFields() {
        createRequest.setSemester(null);
        createRequest.setYear(null);
        createRequest.setCredits(null);
        createRequest.setStatus(null);
        createRequest.setMaxStudents(null);
        createRequest.setDifficulty(null);
        createRequest.setPrice(null);
        createRequest.setTags(null);
        
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));
        
        CourseResponse response = courseService.createCourse(createRequest, teacher.getId());

        assertNotNull(response);
        assertEquals("2024春", response.getSemester());
        assertEquals("intermediate", response.getDifficulty());
    }

    @Test
    @DisplayName("createCourse - Tags Serialization Fails")
    void createCourse_tagsSerializationFails() throws JsonProcessingException {
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> {
            Course c = i.getArgument(0);
            assertEquals("[]", c.getTags()); // Should default to empty array
            return c;
        });
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        courseService.createCourse(createRequest, teacher.getId());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    // =================== updateCourse Tests ===================
    @Test
    @DisplayName("updateCourse - Success by Teacher")
    void updateCourse_successByTeacher() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        
        CourseResponse response = courseService.updateCourse(1L, createRequest, teacher.getId(), UserRole.TEACHER);
        assertEquals("New Course", response.getName());
    }

    @Test
    @DisplayName("updateCourse - Duplicate Code")
    void updateCourse_duplicateCode() {
        createRequest.setCode("EXISTING_CODE");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.findByCode("EXISTING_CODE")).thenReturn(Optional.of(new Course()));
        assertThrows(BusinessException.class, () -> courseService.updateCourse(1L, createRequest, teacher.getId(), UserRole.TEACHER));
    }
    
    @Test
    @DisplayName("updateCourse - Tags Serialization Fails")
    void updateCourse_tagsSerializationFails() throws JsonProcessingException {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertDoesNotThrow(() -> courseService.updateCourse(1L, createRequest, teacher.getId(), UserRole.TEACHER));
    }


    // =================== deleteCourse Tests ===================
    @Test
    @DisplayName("deleteCourse - Success by Admin")
    void deleteCourse_successByAdmin() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        courseService.deleteCourse(1L, admin.getId(), UserRole.ADMIN);
        
        assertFalse(course.getIsActive());
        verify(courseRepository, times(1)).save(course);
        verify(courseEnrollmentRepository, times(1)).batchUnenrollByCourseId(eq(1L), any());
    }

    // =================== enrollCourse Tests ===================
    @Test
    @DisplayName("enrollCourse - Success")
    void enrollCourse_success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.existsByStudentIdAndCourseIdAndIsActiveTrue(student.getId(), 1L)).thenReturn(false);
        when(courseEnrollmentRepository.countByCourseIdAndIsActiveTrue(1L)).thenReturn(10L);
        when(courseEnrollmentRepository.save(any(CourseEnrollment.class))).thenReturn(enrollment);

        Map<String, Object> response = courseService.enrollCourse(1L, student.getId());
        assertTrue(response.get("message").toString().contains("成功"));
    }
    
    @Test
    @DisplayName("enrollCourse - Course not active")
    void enrollCourse_notActive() {
        course.setIsActive(false);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        assertThrows(BusinessException.class, () -> courseService.enrollCourse(1L, student.getId()));
    }

    @Test
    @DisplayName("enrollCourse - Already Enrolled")
    void enrollCourse_alreadyEnrolled() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.existsByStudentIdAndCourseIdAndIsActiveTrue(student.getId(), 1L)).thenReturn(true);
        assertThrows(BusinessException.class, () -> courseService.enrollCourse(1L, student.getId()));
    }

    @Test
    @DisplayName("enrollCourse - Course Full")
    void enrollCourse_courseFull() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.existsByStudentIdAndCourseIdAndIsActiveTrue(student.getId(), 1L)).thenReturn(false);
        when(courseEnrollmentRepository.countByCourseIdAndIsActiveTrue(1L)).thenReturn(50L); // Max students
        assertThrows(BusinessException.class, () -> courseService.enrollCourse(1L, student.getId()));
    }

    // =================== getCourseStudents Tests ===================
    @Test
    @DisplayName("getCourseStudents - Success")
    void getCourseStudents_success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.findByCourseIdAndIsActiveTrue(1L)).thenReturn(List.of(enrollment));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        Map<String, Object> response = courseService.getCourseStudents(1L, teacher.getId(), UserRole.TEACHER);
        assertEquals(1, response.get("total"));
    }
    
    @Test
    @DisplayName("getCourseStudents - Student not found in user repo")
    void getCourseStudents_studentNotFound() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.findByCourseIdAndIsActiveTrue(1L)).thenReturn(List.of(enrollment));
        when(userRepository.findById(student.getId())).thenReturn(Optional.empty()); // Student deleted

        Map<String, Object> response = courseService.getCourseStudents(1L, teacher.getId(), UserRole.TEACHER);
        assertEquals(0, response.get("total")); // Should be filtered out
    }


    // =================== getCourseAnalytics Tests ===================
    @Test
    @DisplayName("getCourseAnalytics - Success")
    void getCourseAnalytics_success() {
        enrollment.setProgress(100.0); // Completed
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.findByCourseIdAndIsActiveTrue(1L)).thenReturn(List.of(enrollment));
        when(taskRepository.countByCourseId(1L)).thenReturn(10L);

        Map<String, Object> analytics = courseService.getCourseAnalytics(1L, teacher.getId(), UserRole.TEACHER);
        assertEquals(1L, analytics.get("totalStudents"));
        assertEquals(100.0, analytics.get("averageProgress"));
        assertEquals(100.0, analytics.get("completionRate"));
        assertEquals(10L, analytics.get("totalTasks"));
    }
    
    @Test
    @DisplayName("getCourseAnalytics - No Enrollments")
    void getCourseAnalytics_noEnrollments() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseEnrollmentRepository.findByCourseIdAndIsActiveTrue(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> analytics = courseService.getCourseAnalytics(1L, teacher.getId(), UserRole.TEACHER);
        assertEquals(0L, analytics.get("totalStudents"));
        assertEquals(0.0, analytics.get("averageProgress"));
        assertEquals(0.0, analytics.get("completionRate"));
    }
}
