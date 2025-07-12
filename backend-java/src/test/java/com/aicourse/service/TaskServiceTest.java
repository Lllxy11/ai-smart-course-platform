import com.aicourse.entity.*;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.TaskRepository;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.SubmissionRepository;
import com.aicourse.dto.TaskCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private TaskService taskService;

    private User teacher;
    private User admin;
    private User student;
    private Course course;
    private Task task;
    private TaskCreateRequest taskCreateRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        teacher = new User();
        teacher.setId(1L);
        teacher.setRole(UserRole.TEACHER);

        admin = new User();
        admin.setId(2L);
        admin.setRole(UserRole.ADMIN);

        student = new User();
        student.setId(3L);
        student.setRole(UserRole.STUDENT);

        course = new Course();
        course.setId(1L);

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setCourseId(course.getId());
        task.setCreatorId(teacher.getId());
        task.setStatus("active");

        taskCreateRequest = new TaskCreateRequest();
        taskCreateRequest.setTitle("New Task");
        taskCreateRequest.setCourseId(course.getId());
        taskCreateRequest.setStartDate(LocalDateTime.now());
        taskCreateRequest.setDueDate(LocalDateTime.now().plusDays(1));
        taskCreateRequest.setMaxScore(100.0);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getTasks_TeacherRole() {
        List<Task> allTasks = Arrays.asList(task);
        when(taskRepository.findAll()).thenReturn(allTasks);

        Page<Map<String, Object>> result = taskService.getTasks(pageable, null, null, null, null, null, null, teacher);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasks_AdminRoleWithTeacherId() {
        List<Task> allTasks = Arrays.asList(task);
        when(taskRepository.findAll()).thenReturn(allTasks);

        Page<Map<String, Object>> result = taskService.getTasks(pageable, null, null, null, null, teacher.getId(), null, admin);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasks_FilterByCourseId() {
        List<Task> allTasks = Arrays.asList(task);
        when(taskRepository.findAll()).thenReturn(allTasks);

        Page<Map<String, Object>> result = taskService.getTasks(pageable, course.getId(), null, null, null, null, null, teacher);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasks_FilterByTaskType() {
        task.setTaskType("assignment");
        List<Task> allTasks = Arrays.asList(task);
        when(taskRepository.findAll()).thenReturn(allTasks);

        Page<Map<String, Object>> result = taskService.getTasks(pageable, null, "assignment", null, null, null, null, teacher);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasks_FilterByKeyword() {
        List<Task> allTasks = Arrays.asList(task);
        when(taskRepository.findAll()).thenReturn(allTasks);

        Page<Map<String, Object>> result = taskService.getTasks(pageable, null, null, "Test", null, null, null, teacher);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasks_FilterByStatus() {
        List<Task> allTasks = Arrays.asList(task);
        when(taskRepository.findAll()).thenReturn(allTasks);

        Page<Map<String, Object>> result = taskService.getTasks(pageable, null, null, null, "active", null, null, teacher);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTaskDetail_StudentRole() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        Map<String, Object> result = taskService.getTaskDetail(task.getId(), student);

        assertNotNull(result);
    }

    @Test
    void getTaskDetail_OtherRole() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        Map<String, Object> result = taskService.getTaskDetail(task.getId(), teacher);

        assertNotNull(result);
    }

    @Test
    void createTask_TeacherRole() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.createTask(taskCreateRequest, teacher);

        assertNotNull(result);
        assertEquals(taskCreateRequest.getTitle(), result.getTitle());
    }

    @Test
    void createTask_AdminRole() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.createTask(taskCreateRequest, admin);

        assertNotNull(result);
        assertEquals(taskCreateRequest.getTitle(), result.getTitle());
    }

    @Test
    void createTask_InsufficientPermissions() {
        assertThrows(BusinessException.class, () -> taskService.createTask(taskCreateRequest, student));
    }

    @Test
    void createTask_CourseNotFound() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(taskCreateRequest, teacher));
    }

    @Test
    void updateTask_TaskNotFound() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(task.getId(), new HashMap<>(), teacher));
    }

    @Test
    void updateTask_InsufficientPermissions() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class, () -> taskService.updateTask(task.getId(), new HashMap<>(), student));
    }

    @Test
    void updateTask_Success() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", "Updated Task");

        Task result = taskService.updateTask(task.getId(), taskData, teacher);

        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
    }

    @Test
    void deleteTask_TaskNotFound() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(task.getId(), teacher));
    }

    @Test
    void deleteTask_InsufficientPermissions() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class, () -> taskService.deleteTask(task.getId(), student));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskService.deleteTask(task.getId(), teacher);

        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void submitTask_TaskNotFound() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.submitTask(task.getId(), new HashMap<>(), student));
    }

    @Test
    void submitTask_PastDueDate() {
        task.setDueDate(LocalDateTime.now().minusDays(1));
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class, () -> taskService.submitTask(task.getId(), new HashMap<>(), student));
    }

    @Test
    void submitTask_Success() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        Map<String, Object> result = taskService.submitTask(task.getId(), new HashMap<>(), student);

        assertNotNull(result);
        assertEquals("任务提交成功", result.get("message"));
    }}
