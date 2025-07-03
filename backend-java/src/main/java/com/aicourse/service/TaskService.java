package com.aicourse.service;

import com.aicourse.entity.*;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.TaskRepository;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.aicourse.dto.TaskCreateRequest;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;

        public Page<Map<String, Object>> getTasks(Pageable pageable, Long courseId, String taskType,
                                             String keyword, String status, Long teacherId, Long studentId, User currentUser) {
        
        // 构建查询条件
        List<Task> allTasks = taskRepository.findAll();
        
        // 调试信息
        System.out.println("=== TaskService Debug ===");
        System.out.println("Current User ID: " + currentUser.getId());
        System.out.println("Current User Role: " + currentUser.getRole());
        System.out.println("TeacherId parameter: " + teacherId);
        System.out.println("Total tasks in DB: " + allTasks.size());
        for (Task task : allTasks) {
            System.out.println("Task ID: " + task.getId() + ", CreatorId: " + task.getCreatorId() + ", Title: " + task.getTitle());
        }
        
        // 根据用户角色过滤任务
        if (currentUser.getRole() == UserRole.TEACHER) {
            // 教师只能看到自己创建的任务
            allTasks = allTasks.stream()
                .filter(task -> task.getCreatorId().equals(currentUser.getId()))
                .collect(Collectors.toList());
            System.out.println("After teacher filter: " + allTasks.size() + " tasks");
        } else if (teacherId != null && currentUser.getRole() == UserRole.ADMIN) {
            // 管理员可以通过teacherId筛选特定教师的任务
            allTasks = allTasks.stream()
                .filter(task -> task.getCreatorId().equals(teacherId))
                .collect(Collectors.toList());
            System.out.println("After admin filter (teacherId=" + teacherId + "): " + allTasks.size() + " tasks");
        }
        
        // 其他过滤条件
        System.out.println("=== Other Filters Debug ===");
        System.out.println("CourseId filter: " + courseId);
        System.out.println("TaskType filter: " + taskType);
        System.out.println("Keyword filter: " + keyword);
        System.out.println("Status filter: " + status);
        
        List<Task> filteredTasks = allTasks.stream()
            .filter(task -> {
                boolean courseMatch = courseId == null || task.getCourseId().equals(courseId);
                if (!courseMatch) System.out.println("Task " + task.getId() + " filtered out by courseId");
                return courseMatch;
            })
            .filter(task -> {
                boolean typeMatch = taskType == null || taskType.trim().isEmpty() || task.getTaskType().equalsIgnoreCase(taskType);
                if (!typeMatch) System.out.println("Task " + task.getId() + " filtered out by taskType. Expected: " + taskType + ", Actual: " + task.getTaskType());
                return typeMatch;
            })
            .filter(task -> {
                boolean keywordMatch = keyword == null || keyword.trim().isEmpty() || task.getTitle().contains(keyword);
                if (!keywordMatch) System.out.println("Task " + task.getId() + " filtered out by keyword");
                return keywordMatch;
            })
            .filter(task -> {
                boolean statusMatch = status == null || status.trim().isEmpty() || task.getStatus().equalsIgnoreCase(status);
                if (!statusMatch) System.out.println("Task " + task.getId() + " filtered out by status. Expected: " + status + ", Actual: " + task.getStatus());
                return statusMatch;
            })
            .collect(Collectors.toList());
            
        System.out.println("Final filtered tasks: " + filteredTasks.size());

        // 分页处理
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredTasks.size());
        List<Task> pageContent = start < filteredTasks.size() ? filteredTasks.subList(start, end) : new ArrayList<>();
        
        // 转换为返回格式，包含提交状态
        List<Map<String, Object>> taskList = pageContent.stream()
            .map(task -> convertTaskToMapWithSubmission(task, studentId != null ? studentId : currentUser.getId(), currentUser))
            .collect(Collectors.toList());
        
        // 如果指定了状态过滤，在转换后再过滤
        if (status != null && !status.trim().isEmpty()) {
            taskList = taskList.stream()
                .filter(taskMap -> status.equals(taskMap.get("status")))
                .collect(Collectors.toList());
        }
        
        return new PageImpl<>(taskList, pageable, taskList.size());
    }

    public Map<String, Object> getTaskDetail(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 如果是学生，包含提交状态信息
        if (currentUser.getRole().name().equals("STUDENT")) {
            return convertTaskToMapWithSubmission(task, currentUser.getId(), currentUser);
        } else {
            return convertTaskToDetailMap(task, currentUser);
        }
    }

    public Task createTask(TaskCreateRequest request, User currentUser) {
        if (!currentUser.getRole().name().equals("TEACHER") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCourseId(request.getCourseId());
        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
            task.setCourse(course);
        }
        task.setTaskType("assignment");
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setMaxScore(request.getMaxScore());
        task.setRequirements(request.getRequirements());
        task.setAttachmentUrl(request.getAttachmentUrl());
        task.setCreatorId(currentUser.getId());
        task.setCreator(currentUser);
        task.setStatus("draft");
        task.setIsPublished(false);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Map<String, Object> taskData, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 权限检查
        if (!task.getCreator().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }
        
        // 更新字段
        if (taskData.containsKey("title")) {
            task.setTitle((String) taskData.get("title"));
        }
        if (taskData.containsKey("description")) {
            task.setDescription((String) taskData.get("description"));
        }
        if (taskData.containsKey("dueDate")) {
            task.setDueDate(LocalDateTime.parse(taskData.get("dueDate").toString()));
        }
        if (taskData.containsKey("maxScore")) {
            task.setMaxScore(Double.valueOf(taskData.get("maxScore").toString()));
        }
        
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 权限检查
        if (!task.getCreator().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }
        
        taskRepository.delete(task);
    }

    public Map<String, Object> submitTask(Long taskId, Map<String, Object> submissionData, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 检查截止时间
        if (task.getDueDate() != null && LocalDateTime.now().isAfter(task.getDueDate())) {
            throw new BusinessException("任务已过截止时间");
        }
        
        // 创建提交记录 - 这里需要Submission实体支持
        Map<String, Object> result = new HashMap<>();
        result.put("message", "任务提交成功");
        result.put("taskId", taskId);
        result.put("submittedAt", LocalDateTime.now().toString());
        
        return result;
    }

    public Map<String, Object> getTaskSubmissions(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 权限检查
        if (!task.getCreator().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("submissions", new ArrayList<>());
        result.put("total", 0);
        result.put("taskTitle", task.getTitle());
        
        return result;
    }

    public Map<String, Object> getTaskSubmissionStatistics(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN") &&
            !task.getCreator().getId().equals(currentUser.getId())) {
            throw new BusinessException("权限不足");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("graded", 0);
        result.put("pending", 0);
        
        return result;
    }

    public Map<String, Object> getTaskActivities(Long taskId, User currentUser) {
        // 验证任务存在
        taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("activities", new ArrayList<>());
        
        return result;
    }

    public void publishTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 权限检查
        if (!task.getCreator().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }
        
        task.setStatus("published");
        task.setIsPublished(true);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    public Map<String, Object> getTaskStatistics(Long courseId, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }
        
        List<Task> tasks = taskRepository.findAll();
        if (courseId != null) {
            tasks = tasks.stream()
                .filter(task -> task.getCourseId().equals(courseId))
                .collect(Collectors.toList());
        }
        
        if (currentUser.getRole().name().equals("TEACHER")) {
            tasks = tasks.stream()
                .filter(task -> task.getCreatorId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTasks", tasks.size());
        statistics.put("publishedTasks", tasks.stream().mapToInt(t -> "PUBLISHED".equals(t.getStatus()) ? 1 : 0).sum());
        statistics.put("draftTasks", tasks.stream().mapToInt(t -> "DRAFT".equals(t.getStatus()) ? 1 : 0).sum());
        
        return statistics;
    }

    public Map<String, Object> getTaskAnalytics(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        
        // 权限检查
        if (!currentUser.getRole().name().equals("ADMIN") && 
            !task.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException("权限不足");
        }
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("taskId", taskId);
        analytics.put("taskTitle", task.getTitle());
        analytics.put("taskType", task.getTaskType());
        analytics.put("status", task.getStatus());
        analytics.put("maxScore", task.getMaxScore());
        analytics.put("dueDate", task.getDueDate());
        analytics.put("createdAt", task.getCreatedAt());
        
        // 提交统计（简化实现）
        analytics.put("totalSubmissions", 0);
        analytics.put("gradedSubmissions", 0);
        analytics.put("pendingSubmissions", 0);
        analytics.put("averageScore", 0.0);
        analytics.put("passRate", 0.0);
        
        // 时间分析
        analytics.put("onTimeSubmissions", 0);
        analytics.put("lateSubmissions", 0);
        analytics.put("noSubmissions", 0);
        
        return analytics;
    }

    private Map<String, Object> convertTaskToMap(Task task, User currentUser) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("id", task.getId());
        taskData.put("title", task.getTitle());
        taskData.put("description", task.getDescription());
        taskData.put("type", task.getTaskType() != null ? task.getTaskType().toUpperCase() : "ASSIGNMENT");
        taskData.put("status", task.getStatus() != null ? task.getStatus().toUpperCase() : "DRAFT");
        taskData.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : "");
        taskData.put("maxScore", task.getMaxScore() != null ? task.getMaxScore() : 100);
        taskData.put("courseId", task.getCourseId());
        
        // 获取课程名称
        String courseName = "未知课程";
        if (task.getCourseId() != null) {
            try {
                Course course = courseRepository.findById(task.getCourseId()).orElse(null);
                if (course != null) {
                    courseName = course.getName();
                }
            } catch (Exception e) {
                // 忽略异常，使用默认值
            }
        }
        taskData.put("courseName", courseName);
        
        taskData.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : "");
        taskData.put("updatedAt", task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : "");
        
        // 提交统计 - 简化实现
        taskData.put("totalStudents", 0);
        taskData.put("submittedCount", 0);
        taskData.put("pendingCount", 0);
        taskData.put("submission", null);
        
        return taskData;
    }
    
    private Map<String, Object> convertTaskToMapWithSubmission(Task task, Long studentId, User currentUser) {
        Map<String, Object> taskData = convertTaskToMap(task, currentUser);
        
        // 查找学生的提交记录
        List<Submission> submissions = submissionRepository.findByTaskIdAndStudentId(task.getId(), studentId);
        
        if (!submissions.isEmpty()) {
            Submission submission = submissions.get(0); // 取最新的提交
            
            // 根据提交状态设置任务状态
            switch (submission.getStatus()) {
                case SUBMITTED:
                    taskData.put("status", "submitted");
                    break;
                case GRADED:
                    taskData.put("status", "graded");
                    taskData.put("score", submission.getScore());
                    taskData.put("totalScore", submission.getMaxScore());
                    break;
                case LATE:
                    taskData.put("status", "overdue");
                    break;
                default:
                    taskData.put("status", "pending");
            }
            
            // 添加提交相关信息
            taskData.put("submittedAt", submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : null);
            taskData.put("gradedAt", submission.getGradedAt() != null ? submission.getGradedAt().toString() : null);
            taskData.put("feedback", submission.getFeedback());
            taskData.put("progress", submission.getStatus() == Submission.SubmissionStatus.GRADED ? 100 : 80);
            
        } else {
            // 没有提交记录，检查是否过期
            if (task.getDueDate() != null && LocalDateTime.now().isAfter(task.getDueDate())) {
                taskData.put("status", "overdue");
            } else {
                taskData.put("status", "pending");
            }
            taskData.put("progress", 0);
        }
        
        // 添加时间相关信息
        if (task.getDueDate() != null) {
            taskData.put("deadline", task.getDueDate().toString());
            taskData.put("isUrgent", LocalDateTime.now().plusHours(24).isAfter(task.getDueDate()));
        }
        
        // 添加显示用的时间格式
        if (task.getCreatedAt() != null) {
            taskData.put("publishTime", task.getCreatedAt().toString());
        }
        
        return taskData;
    }

    private Map<String, Object> convertTaskToDetailMap(Task task, User currentUser) {
        Map<String, Object> taskData = convertTaskToMap(task, currentUser);
        
        // 添加详细信息
        taskData.put("taskType", task.getTaskType() != null ? task.getTaskType().toLowerCase() : "assignment");
        taskData.put("dueTime", task.getDueDate());
        taskData.put("startTime", null); // 当前Task实体中没有这个字段
        taskData.put("closeTime", null); // 当前Task实体中没有这个字段
        taskData.put("creatorId", task.getCreator() != null ? task.getCreator().getId() : task.getCreatorId());
        taskData.put("allowLateSubmission", false); // 当前Task实体中没有这个字段
        taskData.put("latePenalty", 0); // 当前Task实体中没有这个字段
        taskData.put("maxAttempts", 1); // 当前Task实体中没有这个字段
        taskData.put("autoGrading", false); // 当前Task实体中没有这个字段
        taskData.put("aiGuidance", false); // 当前Task实体中没有这个字段
        
        return taskData;
    }
} 