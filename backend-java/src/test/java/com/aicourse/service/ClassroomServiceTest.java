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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private User teacherAsAdvisor;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN);

        teacherUser = new User();
        teacherUser.setId(2L);
        teacherUser.setRole(UserRole.TEACHER);

        studentUser = new User();
        studentUser.setId(3L);
        studentUser.setRole(UserRole.STUDENT);
        
        teacherAsAdvisor = new User();
        teacherAsAdvisor.setId(4L);
        teacherAsAdvisor.setRole(UserRole.TEACHER);
        teacherAsAdvisor.setFullName("Advisor Name");

        classroom = new Classroom();
        classroom.setId(101L);
        classroom.setName("CS 101");
        classroom.setCode("CS101-2024");
        classroom.setAdvisorId(teacherAsAdvisor.getId());
        classroom.setMaxStudents(50);
        
        studentInClass = new User();
        studentInClass.setId(5L);
        studentInClass.setRole(UserRole.STUDENT);
        studentInClass.setClassId(classroom.getId());
    }

    @Test
    @DisplayName("Get Classrooms - Success for Admin")
    void getClassrooms_SuccessForAdmin() {
        // Arrange
        Page<Classroom> classroomPage = new PageImpl<>(List.of(classroom));
        when(classroomRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(classroomPage);

        // Act
        Page<Classroom> result = classroomService.getClassrooms(Pageable.unpaged(), null, null, null, adminUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(classroomRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Get Classrooms - Permission Denied for Student")
    void getClassrooms_PermissionDeniedForStudent() {
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.getClassrooms(Pageable.unpaged(), null, null, null, studentUser);
        });
        assertEquals("权限不足", exception.getMessage());
    }

    @Test
    @DisplayName("Get Classroom Detail - Success for Teacher")
    void getClassroomDetail_SuccessForTeacher() {
        // Arrange
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(userRepository.findByClassIdAndRoleAndIsActiveTrue(classroom.getId(), UserRole.STUDENT)).thenReturn(List.of(studentInClass));
        when(userRepository.findById(teacherAsAdvisor.getId())).thenReturn(Optional.of(teacherAsAdvisor));

        // Act
        Map<String, Object> result = classroomService.getClassroomDetail(classroom.getId(), teacherUser);

        // Assert
        assertNotNull(result);
        assertEquals("CS 101", result.get("name"));
        assertEquals("Advisor Name", result.get("advisor_name"));
        assertEquals(1, ((List<?>) result.get("students")).size());
    }
    
    @Test
    @DisplayName("Get Classroom Detail - Classroom Not Found")
    void getClassroomDetail_NotFound() {
        // Arrange
        when(classroomRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            classroomService.getClassroomDetail(999L, adminUser);
        });
    }

    @Test
    @DisplayName("Create Classroom - Success as Admin")
    void createClassroom_SuccessAsAdmin() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("name", "New Class");
        data.put("code", "NC2025");
        data.put("advisor_id", teacherAsAdvisor.getId());
        
        when(classroomRepository.existsByCode("NC2025")).thenReturn(false);
        when(userRepository.findById(teacherAsAdvisor.getId())).thenReturn(Optional.of(teacherAsAdvisor));
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Classroom result = classroomService.createClassroom(data, adminUser);

        // Assert
        assertNotNull(result);
        assertEquals("New Class", result.getName());
        assertEquals(teacherAsAdvisor.getId(), result.getAdvisorId());
        verify(classroomRepository, times(1)).save(any(Classroom.class));
    }
    
    @Test
    @DisplayName("Create Classroom - Permission Denied for Teacher")
    void createClassroom_PermissionDeniedForTeacher() {
        // Arrange
        Map<String, Object> data = new HashMap<>();

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.createClassroom(data, teacherUser);
        });
        assertEquals("权限不足", exception.getMessage());
    }

    @Test
    @DisplayName("Create Classroom - Code Already Exists")
    void createClassroom_CodeExists() {
        // Arrange
        Map<String, Object> data = Map.of("code", "CS101-2024");
        when(classroomRepository.existsByCode("CS101-2024")).thenReturn(true);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.createClassroom(data, adminUser);
        });
        assertEquals("班级代码已存在", exception.getMessage());
    }

    @Test
    @DisplayName("Update Classroom - Success as Admin")
    void updateClassroom_SuccessAsAdmin() {
        // Arrange
        Map<String, Object> data = Map.of("name", "Updated CS 101");
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Classroom result = classroomService.updateClassroom(classroom.getId(), data, adminUser);

        // Assert
        assertEquals("Updated CS 101", result.getName());
        assertNotNull(result.getUpdatedAt());
        verify(classroomRepository, times(1)).save(any(Classroom.class));
    }

    @Test
    @DisplayName("Delete Classroom - Success (Soft Delete)")
    void deleteClassroom_Success() {
        // Arrange
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(classroom.getId(), UserRole.STUDENT)).thenReturn(0L);

        // Act
        classroomService.deleteClassroom(classroom.getId(), adminUser);

        // Assert
        ArgumentCaptor<Classroom> captor = ArgumentCaptor.forClass(Classroom.class);
        verify(classroomRepository).save(captor.capture());
        
        Classroom savedClassroom = captor.getValue();
        assertFalse(savedClassroom.getIsActive());
        assertNotNull(savedClassroom.getUpdatedAt());
    }

    @Test
    @DisplayName("Delete Classroom - Fails if Students Exist")
    void deleteClassroom_FailsWithStudents() {
        // Arrange
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(classroom.getId(), UserRole.STUDENT)).thenReturn(1L);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.deleteClassroom(classroom.getId(), adminUser);
        });
        assertEquals("班级中还有学生，无法删除", exception.getMessage());
    }

    @Test
    @DisplayName("Add Student to Classroom - Success")
    void addStudentToClassroom_Success() {
        // Arrange
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(classroom.getId(), UserRole.STUDENT)).thenReturn(10L); // Not full

        // Act
        classroomService.addStudentToClassroom(classroom.getId(), studentUser.getId(), adminUser);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(classroom.getId(), userCaptor.getValue().getClassId());
        
        ArgumentCaptor<Classroom> classroomCaptor = ArgumentCaptor.forClass(Classroom.class);
        verify(classroomRepository).save(classroomCaptor.capture());
        assertEquals(11, classroomCaptor.getValue().getStudentCount());
    }

    @Test
    @DisplayName("Add Student to Classroom - Fails if Class is Full")
    void addStudentToClassroom_ClassIsFull() {
        // Arrange
        classroom.setMaxStudents(10);
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(classroom.getId(), UserRole.STUDENT)).thenReturn(10L); // Full

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.addStudentToClassroom(classroom.getId(), studentUser.getId(), adminUser);
        });
        assertEquals("班级人数已满", exception.getMessage());
    }
    
    @Test
    @DisplayName("Add Student to Classroom - Fails if Student already in another class")
    void addStudentToClassroom_StudentInAnotherClass() {
        // Arrange
        studentUser.setClassId(999L); // Already in another class
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(userRepository.findById(studentUser.getId())).thenReturn(Optional.of(studentUser));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.addStudentToClassroom(classroom.getId(), studentUser.getId(), adminUser);
        });
        assertEquals("学生已在其他班级中", exception.getMessage());
    }

    @Test
    @DisplayName("Remove Student From Classroom - Success")
    void removeStudentFromClassroom_Success() {
        // Arrange
        when(userRepository.findByIdAndClassIdAndRole(studentInClass.getId(), classroom.getId(), UserRole.STUDENT)).thenReturn(studentInClass);
        when(classroomRepository.findById(classroom.getId())).thenReturn(Optional.of(classroom));
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(classroom.getId(), UserRole.STUDENT)).thenReturn(0L); // Simulates count after removal

        // Act
        classroomService.removeStudentFromClassroom(classroom.getId(), studentInClass.getId(), adminUser);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNull(userCaptor.getValue().getClassId());

        ArgumentCaptor<Classroom> classroomCaptor = ArgumentCaptor.forClass(Classroom.class);
        verify(classroomRepository).save(classroomCaptor.capture());
        assertEquals(0, classroomCaptor.getValue().getStudentCount());
    }

    @Test
    @DisplayName("Remove Student From Classroom - Fails if Student Not in Class")
    void removeStudentFromClassroom_StudentNotInClass() {
        // Arrange
        when(userRepository.findByIdAndClassIdAndRole(anyLong(), anyLong(), any(UserRole.class))).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            classroomService.removeStudentFromClassroom(classroom.getId(), 999L, adminUser);
        });
        assertEquals("学生不在指定班级中", exception.getMessage());
    }
    
    @Test
    @DisplayName("Format Classroom List - Success")
    void formatClassroomList_Success() {
        // Arrange
        when(userRepository.countByClassIdAndRoleAndIsActiveTrue(classroom.getId(), UserRole.STUDENT)).thenReturn(25L);
        when(userRepository.findById(teacherAsAdvisor.getId())).thenReturn(Optional.of(teacherAsAdvisor));
        
        // Act
        List<Map<String, Object>> result = classroomService.formatClassroomList(List.of(classroom));
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        Map<String, Object> formattedClass = result.get(0);
        assertEquals(25, formattedClass.get("student_count"));
        assertEquals("Advisor Name", formattedClass.get("advisor_name"));
    }
}