package com.aicourse.service;

import com.aicourse.entity.Classroom;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.ClassroomRepository;
import com.aicourse.repository.UserRepository;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClassroomService Unit Tests")
class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClassroomService classroomService;

    private User adminUser;
    private User teacherUser;
    private User studentUser;
    private Classroom classroom;
    private User studentInClass;
    private User teacherAdvisor;

    @BeforeEach
    void setUp() {
        adminUser = createUser(1L, "admin", UserRole.ADMIN);
        teacherUser = createUser(2L, "teacher", UserRole.TEACHER);
        studentUser = createUser(3L, "student", UserRole.STUDENT);

        teacherAdvisor = createUser(4L, "advisor", UserRole.TEACHER);
        teacherAdvisor.setFullName("Advisor Name");

        classroom = new Classroom();
        classroom.setId(1L);
        classroom.setName("Test Class");
        classroom.setCode("CS101");
        classroom.setAdvisorId(teacherAdvisor.getId());
        classroom.setMaxStudents(50);
        classroom.setIsActive(true);
        
        studentInClass = createUser(5L, "studentInClass", UserRole.STUDENT);
        studentInClass.setClassId(classroom.getId());
    }

    private User createUser(Long id, String username, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    // =================== getClassrooms Tests ===================

    @Test
    @DisplayName("getClassrooms - Success for Admin")
    void getClassrooms_successForAdmin() {
        Page<Classroom> classroomPage = new PageImpl<>(Collections.singletonList(classroom));
        when(classroomRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(classroomPage);

        Page<Classroom> result = classroomService.getClassrooms(Pageable.unpaged(), null, null, null, adminUser);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(classroomRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getClassrooms - Success for Teacher")
    void getClassrooms_successForTeacher() {
        Page<Classroom> classroomPage = new PageImpl<>(Collections.singletonList(classroom));
        when(classroomRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(classroomPage);

        Page<Classroom> result = classroomService.getClassrooms(Pageable.unpaged(), "Test", "2023", "CS", teacherUser);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("getClassrooms - Access Denied for Student")
    void getClassrooms_accessDeniedForStudent() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.getClassrooms(Pageable.unpaged(), null, null, null, studentUser);
        });
        assertEquals("权限不足", exception.getMessage());
    }

    // =================== getClassroomDetail Tests ===================

    @Test
    @DisplayName("getClassroomDetail - Success")
    void getClassroomDetail_success() {
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(userRepository.findByClassIdAndRoleAndIsActiveTrue(1L, UserRole.STUDENT))
            .thenReturn(Collections.singletonList(studentInClass));
        when(userRepository.findById(teacherAdvisor.getId())).thenReturn(Optional.of(teacherAdvisor));

        Map<String, Object> result = classroomService.getClassroomDetail(1L, adminUser);

        assertNotNull(result);
        assertEquals("Test Class", result.get("name"));
        assertEquals("Advisor Name", result.get("advisor_name"));
        assertEquals(1, ((List<?>) result.get("students")).size());
    }

    @Test
    @DisplayName("getClassroomDetail - Classroom Not Found")
    void getClassroomDetail_notFound() {
        when(classroomRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> classroomService.getClassroomDetail(99L, adminUser));
    }

    // =================== createClassroom Tests ===================

    @Test
    @DisplayName("createClassroom - Success by Admin")
    void createClassroom_success() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "New Class");
        data.put("code", "NC101");
        data.put("advisor_id", teacherAdvisor.getId());
        
        when(classroomRepository.existsByCode("NC101")).thenReturn(false);
        when(userRepository.findById(teacherAdvisor.getId())).thenReturn(Optional.of(teacherAdvisor));
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(i -> i.getArgument(0));

        Classroom result = classroomService.createClassroom(data, adminUser);

        assertNotNull(result);
        assertEquals("New Class", result.getName());
        verify(classroomRepository, times(1)).save(any(Classroom.class));
    }

    @Test
    @DisplayName("createClassroom - Access Denied for Teacher")
    void createClassroom_accessDenied() {
        Map<String, Object> data = new HashMap<>();
        assertThrows(BusinessException.class, () -> classroomService.createClassroom(data, teacherUser));
    }

    @Test
    @DisplayName("createClassroom - Duplicate Code")
    void createClassroom_duplicateCode() {
        Map<String, Object> data = Collections.singletonMap("code", "CS101");
        when(classroomRepository.existsByCode("CS101")).thenReturn(true);
        assertThrows(BusinessException.class, () -> classroomService.createClassroom(data, adminUser));
    }
    
    @Test
    @DisplayName("createClassroom - Invalid Advisor")
    void createClassroom_invalidAdvisor() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", "NC101");
        data.put("advisor_id", 99L); // Non-existent advisor
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> classroomService.createClassroom(data, adminUser));
    }

    // =================== updateClassroom Tests ===================
    
    @Test
    @DisplayName("updateClassroom - Success by Admin")
    void updateClassroom_success() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Updated Class Name");
        data.put("code", "CS101-UPDATED");
        
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(classroomRepository.existsByCodeAndIdNot("CS101-UPDATED", 1L)).thenReturn(false);
        when(classroomRepository.save(any(Classroom.class))).thenReturn(classroom);

        Classroom result = classroomService.updateClassroom(1L, data, adminUser);

        assertEquals("Updated Class Name", result.getName());
        assertEquals("CS101-UPDATED", result.getCode());
    }

    @Test
    @DisplayName("updateClassroom - Duplicate Code on Update")
    void updateClassroom_duplicateCode() {
        Map<String, Object> data = Collections.singletonMap("code", "EXISTING-CODE");
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(classroomRepository.existsByCodeAndIdNot("EXISTING-CODE", 1L)).thenReturn(true);
        
        assertThrows(BusinessException.class, () -> classroomService.updateClassroom(1L, data, adminUser));
    }

    // =================== deleteClassroom Tests ===================

    @Test
    @DisplayName("deleteClassroom - Success on Empty Class")
    void deleteClassroom_success() {
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(1L, UserRole.STUDENT)).thenReturn(0L);

        classroomService.deleteClassroom(1L, adminUser);

        assertFalse(classroom.getIsActive());
        verify(classroomRepository, times(1)).save(classroom);
    }

    @Test
    @DisplayName("deleteClassroom - Fails on Non-Empty Class")
    void deleteClassroom_failsNonEmpty() {
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(1L, UserRole.STUDENT)).thenReturn(10L);

        assertThrows(BusinessException.class, () -> classroomService.deleteClassroom(1L, adminUser));
    }

    // =================== addStudentToClassroom Tests ===================
    
    @Test
    @DisplayName("addStudentToClassroom - Success")
    void addStudentToClassroom_success() {
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(1L, UserRole.STUDENT)).thenReturn(10L);

        classroomService.addStudentToClassroom(1L, studentUser.getId(), adminUser);

        assertEquals(1L, studentUser.getClassId());
        verify(userRepository, times(1)).save(studentUser);
        verify(classroomRepository, times(1)).save(classroom);
    }

    @Test
    @DisplayName("addStudentToClassroom - Class is Full")
    void addStudentToClassroom_classFull() {
        classroom.setMaxStudents(10);
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(1L, UserRole.STUDENT)).thenReturn(10L);

        assertThrows(BusinessException.class, () -> classroomService.addStudentToClassroom(1L, studentUser.getId(), adminUser));
    }

    @Test
    @DisplayName("addStudentToClassroom - Student Already in Another Class")
    void addStudentToClassroom_studentInAnotherClass() {
        studentUser.setClassId(2L);
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));

        assertThrows(BusinessException.class, () -> classroomService.addStudentToClassroom(1L, studentUser.getId(), adminUser));
    }
    
    // =================== removeStudentFromClassroom Tests ===================

    @Test
    @DisplayName("removeStudentFromClassroom - Success")
    void removeStudentFromClassroom_success() {
        when(userRepository.findByIdAndClassIdAndRole(studentInClass.getId(), 1L, UserRole.STUDENT))
            .thenReturn(studentInClass);
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        
        classroomService.removeStudentFromClassroom(1L, studentInClass.getId(), adminUser);

        assertNull(studentInClass.getClassId());
        verify(userRepository, times(1)).save(studentInClass);
        verify(classroomRepository, times(1)).save(classroom);
    }

    @Test
    @DisplayName("removeStudentFromClassroom - Student Not in Class")
    void removeStudentFromClassroom_studentNotInClass() {
        when(userRepository.findByIdAndClassIdAndRole(studentUser.getId(), 1L, UserRole.STUDENT)).thenReturn(null);
        
        assertThrows(BusinessException.class, () -> classroomService.removeStudentFromClassroom(1L, studentUser.getId(), adminUser));
    }

    // =================== formatClassroomList Tests ===================

    @Test
    @DisplayName("formatClassroomList - Success")
    void formatClassroomList_success() {
        List<Classroom> classrooms = Collections.singletonList(classroom);
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(1L, UserRole.STUDENT)).thenReturn(25L);
        when(userRepository.findById(teacherAdvisor.getId())).thenReturn(Optional.of(teacherAdvisor));

        List<Map<String, Object>> result = classroomService.formatClassroomList(classrooms);

        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> formattedClass = result.get(0);
        assertEquals(25, formattedClass.get("student_count"));
        assertEquals("Advisor Name", formattedClass.get("advisor_name"));
    }
}
