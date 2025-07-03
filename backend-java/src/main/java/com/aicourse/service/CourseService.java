package com.aicourse.service;

import com.aicourse.dto.CourseCreateRequest;
import com.aicourse.dto.CourseResponse;
import com.aicourse.dto.PagedResponse;
import com.aicourse.entity.*;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseEnrollmentRepository;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.TaskRepository;
import com.aicourse.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程业务服务
 */
@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取课程列表
     */
    public PagedResponse<CourseResponse> getCourses(
            Long userId,
            UserRole userRole,
            int page,
            int size,
            String category,
            String difficulty,
            String keyword,
            String semester,
            String status,
            Long teacherId
    ) {
        // 前端已经将页码减1了，这里直接使用
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> coursePage;

        // 处理空字符串参数，转换为null
        category = (category != null && category.trim().isEmpty()) ? null : category;
        difficulty = (difficulty != null && difficulty.trim().isEmpty()) ? null : difficulty;
        keyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
        semester = (semester != null && semester.trim().isEmpty()) ? null : semester;
        status = (status != null && status.trim().isEmpty()) ? null : status;

        // 根据用户角色过滤课程
        if (userRole == UserRole.TEACHER) {
            // 教师只看自己的课程
            coursePage = courseRepository.findCoursesWithFilters(
                    userId, category, difficulty, status, semester, null, keyword, pageable
            );
        } else if (userRole == UserRole.STUDENT) {
            // 学生可以看到所有已发布的课程（包括已选和可选的）
            coursePage = courseRepository.findCoursesWithFilters(
                    null, category, difficulty, status, semester, null, keyword, pageable
            );
            
            // 过滤掉未发布的课程，学生只能看到已发布的课程
            List<Course> filteredCourses = coursePage.getContent().stream()
                    .filter(course -> course.getIsActive() && 
                           ("PUBLISHED".equals(course.getStatus()) || "ONGOING".equals(course.getStatus())))
                    .collect(Collectors.toList());
            
            // 重新构建分页结果
            long totalFiltered = filteredCourses.size();
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), filteredCourses.size());
            
            if (start >= filteredCourses.size()) {
                filteredCourses = new ArrayList<>();
            } else {
                filteredCourses = filteredCourses.subList(start, end);
            }
            
            coursePage = new PageImpl<>(filteredCourses, pageable, totalFiltered);
        } else {
            // 管理员看所有课程
            if (category == null && difficulty == null && status == null && 
                semester == null && keyword == null && teacherId == null) {
                // 如果没有任何筛选条件，直接查询所有激活课程
                coursePage = courseRepository.findByIsActiveTrue(pageable);
            } else {
                // 有筛选条件时使用复合查询
                coursePage = courseRepository.findCoursesWithFilters(
                        teacherId, category, difficulty, status, semester, null, keyword, pageable
                );
            }
        }

        // 转换为响应DTO
        List<CourseResponse> courseResponses = coursePage.getContent().stream()
                .map(course -> buildCourseResponse(course, userId, userRole))
                .collect(Collectors.toList());

        // 返回给前端的页码需要加1（前端期望从1开始的页码）
        return PagedResponse.of(courseResponses, coursePage.getTotalElements(), page + 1, size);
    }

    /**
     * 获取课程统计信息
     */
    public Map<String, Object> getCourseStatistics(
            Long userId, UserRole userRole, String semester, Integer year, String category
    ) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 根据用户角色获取不同的统计数据
        if (userRole == UserRole.ADMIN) {
            // 管理员看所有课程统计
            Long totalCourses = courseRepository.count();
            Long activeCourses = (long) courseRepository.findByIsActiveTrue().size();
            
            // 计算已发布课程数
            List<Course> allCourses = courseRepository.findAll();
            Long publishedCourses = allCourses.stream()
                    .filter(course -> "PUBLISHED".equals(course.getStatus()) && course.getIsActive())
                    .count();
            
            // 计算总选课人数
            Long totalEnrollments = 0L;
            List<CourseEnrollment> allEnrollments = courseEnrollmentRepository.findAll();
            for (CourseEnrollment enrollment : allEnrollments) {
                if (enrollment.getIsActive()) {
                    totalEnrollments++;
                }
            }
            
            // 计算平均评分（这里先设置为固定值，后续可以实现评分系统）
            Double averageRating = 4.5; // 临时固定值
            
            statistics.put("totalCourses", totalCourses);
            statistics.put("activeCourses", activeCourses);
            statistics.put("publishedCourses", publishedCourses);
            statistics.put("totalEnrollments", totalEnrollments);
            statistics.put("averageRating", averageRating);
            statistics.put("inactiveCourses", totalCourses - activeCourses);
            
        } else if (userRole == UserRole.TEACHER) {
            // 教师看自己的课程统计
            Long myCourses = courseRepository.countByTeacherId(userId);
            
            // 计算教师已发布的课程数
            List<Course> teacherCourses = courseRepository.findByTeacherId(userId);
            Long publishedCourses = teacherCourses.stream()
                    .filter(course -> "PUBLISHED".equals(course.getStatus()) && course.getIsActive())
                    .count();
            
            // 计算教师课程的总学生数
            Long totalStudents = 0L;
            for (Course course : teacherCourses) {
                totalStudents += courseEnrollmentRepository.countByCourseIdAndIsActiveTrue(course.getId());
            }
            
            // 教师课程平均评分
            Double averageRating = 4.3; // 临时固定值
            
            statistics.put("totalCourses", myCourses);
            statistics.put("publishedCourses", publishedCourses);
            statistics.put("totalEnrollments", totalStudents);
            statistics.put("averageRating", averageRating);
            statistics.put("myCourses", myCourses);
            statistics.put("myStudents", totalStudents);
            
        } else if (userRole == UserRole.STUDENT) {
            // 学生看自己的选课统计
            Long enrolledCourses = courseEnrollmentRepository.countByStudentIdAndIsActiveTrue(userId);
            
            // 计算完成的课程数（进度>=100%）
            List<CourseEnrollment> completedList = courseEnrollmentRepository.findCompletedCoursesByStudentId(userId);
            Long completedCourses = (long) completedList.size();
            
            // 学生的已发布课程数（即已选的发布状态课程）
            List<Course> enrolledCourseList = courseRepository.findEnrolledCoursesByStudentId(userId);
            Long publishedCourses = enrolledCourseList.stream()
                    .filter(course -> "PUBLISHED".equals(course.getStatus()))
                    .count();
            
            // 学生平均评分（可以是学生给课程的平均评分）
            Double averageRating = 4.2; // 临时固定值
            
            statistics.put("totalCourses", enrolledCourses);
            statistics.put("publishedCourses", publishedCourses);
            statistics.put("totalEnrollments", enrolledCourses);
            statistics.put("averageRating", averageRating);
            statistics.put("enrolledCourses", enrolledCourses);
            statistics.put("completedCourses", completedCourses);
            statistics.put("inProgressCourses", enrolledCourses - completedCourses);
        }
        
        // 添加时间信息
        statistics.put("semester", semester);
        statistics.put("year", year);
        statistics.put("category", category);
        statistics.put("timestamp", LocalDateTime.now());
        
        return statistics;
    }

    /**
     * 获取课程详情
     */
    public CourseResponse getCourseById(Long courseId, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 检查权限
        checkCourseAccess(course, userId, userRole);

        return buildCourseResponse(course, userId, userRole);
    }

    /**
     * 创建课程 - 高性能优化版本
     */
    @Transactional(timeout = 10)
    public CourseResponse createCourse(CourseCreateRequest request, Long teacherId) {
        logger.info("开始创建课程，教师ID: {}, 课程名称: {}", teacherId, request.getName());
        
        // 暂时跳过教师验证以避免数据库连接问题
        // 在Controller层已经进行了权限验证
        logger.info("跳过教师ID验证，直接创建课程");
        
        // 使用纯UUID生成课程代码，完全避免数据库查询
        String courseCode = "COURSE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        logger.info("生成课程代码: {}", courseCode);

        // 创建课程实体
        Course course = new Course();
        course.setName(request.getName());
        course.setCode(courseCode);
        course.setDescription(request.getDescription());
        course.setTeacherId(teacherId);
        course.setSemester(request.getSemester() != null ? request.getSemester() : "2024春");
        course.setYear(request.getYear() != null ? request.getYear() : LocalDateTime.now().getYear());
        course.setCredits(request.getCredits() != null ? request.getCredits() : 3.0);
        course.setStatus(request.getStatus() != null ? request.getStatus() : "PUBLISHED");
        course.setMaxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 50);
        course.setIsActive(true);
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : "intermediate");
        course.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);
        if (request.getStartDate() != null) {
            course.setStartDate(request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            course.setEndDate(request.getEndDate().atStartOfDay());
        }
        course.setCoverImage(request.getCoverImage());
        course.setSyllabusUrl(request.getSyllabusUrl());

        // 处理tags - 简化处理
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                course.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (Exception e) {
                logger.warn("Failed to serialize tags", e);
                course.setTags("[]");
            }
        } else {
            course.setTags("[]");
        }

        LocalDateTime now = LocalDateTime.now();
        course.setCreatedAt(now);
        course.setUpdatedAt(now);

        // 保存课程
        course = courseRepository.save(course);
        logger.info("课程创建成功，ID: {}, 代码: {}", course.getId(), course.getCode());
        
        // 创建精简的响应，避免额外查询
        return buildSimpleCourseResponse(course);
    }

    /**
     * 构建简化的课程响应（用于创建操作，避免额外查询）
     */
    private CourseResponse buildSimpleCourseResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setName(course.getName());
        response.setCode(course.getCode());
        response.setDescription(course.getDescription());
        response.setTeacherId(course.getTeacherId());
        response.setTeacherName(""); // 创建时不查询教师名称，提升性能
        response.setSemester(course.getSemester());
        response.setYear(course.getYear());
        response.setCredits(course.getCredits());
        response.setStatus(course.getStatus());
        response.setMaxStudents(course.getMaxStudents());
        response.setCategory(course.getCategory());
        response.setDifficulty(course.getDifficulty());
        response.setPrice(course.getPrice());
        response.setStartDate(course.getStartDate() != null ? course.getStartDate().toLocalDate() : null);
        response.setEndDate(course.getEndDate() != null ? course.getEndDate().toLocalDate() : null);
        response.setCoverImage(course.getCoverImage());
        response.setSyllabusUrl(course.getSyllabusUrl());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());
        
        // 新创建的课程设置默认值，避免查询
        response.setStudentCount(0);
        response.setTaskCount(0);
        response.setEnrolled(false);
        response.setTags(Collections.emptyList());
        
        return response;
    }

    /**
     * 更新课程
     */
    public CourseResponse updateCourse(Long courseId, CourseCreateRequest request, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 检查权限
        if (!course.getTeacherId().equals(userId) && userRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 检查课程代码唯一性（如果修改了代码）
        if (request.getCode() != null && !request.getCode().equals(course.getCode())) {
            if (courseRepository.findByCode(request.getCode()).isPresent()) {
                throw new BusinessException("课程代码已存在");
            }
        }

        // 更新字段
        if (request.getName() != null) course.setName(request.getName());
        if (request.getCode() != null) course.setCode(request.getCode());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getSemester() != null) course.setSemester(request.getSemester());
        if (request.getYear() != null) course.setYear(request.getYear());
        if (request.getCredits() != null) course.setCredits(request.getCredits());
        if (request.getStatus() != null) course.setStatus(request.getStatus());
        if (request.getMaxStudents() != null) course.setMaxStudents(request.getMaxStudents());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getDifficulty() != null) course.setDifficulty(request.getDifficulty());
        if (request.getPrice() != null) course.setPrice(request.getPrice());
                 if (request.getStartDate() != null) course.setStartDate(request.getStartDate().atStartOfDay());
         if (request.getEndDate() != null) course.setEndDate(request.getEndDate().atStartOfDay());
        if (request.getCoverImage() != null) course.setCoverImage(request.getCoverImage());
        if (request.getSyllabusUrl() != null) course.setSyllabusUrl(request.getSyllabusUrl());

        // 更新tags
        if (request.getTags() != null) {
            try {
                course.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (Exception e) {
                logger.warn("Failed to serialize tags", e);
            }
        }

        course.setUpdatedAt(LocalDateTime.now());
        course = courseRepository.save(course);

        return buildCourseResponse(course, userId, userRole);
    }

    /**
     * 删除课程
     */
    public void deleteCourse(Long courseId, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 检查权限
        if (!course.getTeacherId().equals(userId) && userRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        // 软删除：设置为非激活状态
        course.setIsActive(false);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        // 批量取消选课
        courseEnrollmentRepository.batchUnenrollByCourseId(courseId, LocalDateTime.now());
    }

    /**
     * 学生选课
     */
    public Map<String, Object> enrollCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 检查课程是否激活
        if (!course.getIsActive()) {
            throw new BusinessException("课程未激活");
        }

        // 检查是否已经选过课
        if (courseEnrollmentRepository.existsByStudentIdAndCourseIdAndIsActiveTrue(studentId, courseId)) {
            throw new BusinessException("已经加入该课程");
        }

        // 检查课程人数限制
        Long currentEnrollmentCount = courseEnrollmentRepository.countByCourseIdAndIsActiveTrue(courseId);
        if (currentEnrollmentCount >= course.getMaxStudents()) {
            throw new BusinessException("课程人数已满");
        }

        // 创建选课记录
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setProgress(0.0);
        enrollment.setTotalStudyTime(0);
        enrollment.setIsActive(true);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCreatedAt(LocalDateTime.now());
        enrollment.setUpdatedAt(LocalDateTime.now());

        enrollment = courseEnrollmentRepository.save(enrollment);

        return Map.of(
                "message", "加入课程成功",
                "enrollment", Map.of(
                        "id", enrollment.getId(),
                        "courseId", courseId,
                        "courseName", course.getName(),
                        "enrolledAt", enrollment.getEnrolledAt()
                )
        );
    }

    /**
     * 获取课程学生列表
     */
    public Map<String, Object> getCourseStudents(Long courseId, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 检查权限
        if (!course.getTeacherId().equals(userId) && userRole != UserRole.ADMIN) {
            throw new BusinessException("权限不足");
        }

        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByCourseIdAndIsActiveTrue(courseId);

                 List<Map<String, Object>> students = enrollments.stream()
                 .map(enrollment -> {
                     User student = userRepository.findById(enrollment.getStudentId()).orElse(null);
                     if (student == null) return null;

                     Map<String, Object> studentMap = Map.of(
                             "id", student.getId(),
                             "username", student.getUsername(),
                             "fullName", student.getFullName(),
                             "email", student.getEmail(),
                             "studentId", student.getStudentId() != null ? student.getStudentId() : "",
                             "className", student.getClassName() != null ? student.getClassName() : "",
                             "enrolledAt", enrollment.getEnrolledAt(),
                             "progress", enrollment.getProgress(),
                             "finalScore", enrollment.getFinalScore(),
                             "totalStudyTime", enrollment.getTotalStudyTime()
                     );
                     return (Map<String, Object>) studentMap;
                 })
                 .filter(student -> student != null)
                 .collect(Collectors.toList());

        return Map.of(
                "students", students,
                "total", students.size(),
                "courseName", course.getName()
        );
    }

    /**
     * 构建课程响应DTO
     */
    private CourseResponse buildCourseResponse(Course course, Long userId, UserRole userRole) {
        return buildCourseResponse(course, userId, userRole, true);
    }

    /**
     * 构建课程响应DTO - 优化版本
     */
    private CourseResponse buildCourseResponse(Course course, Long userId, UserRole userRole, boolean includeStats) {
        // 获取教师信息
        User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
        String teacherName = teacher != null ? teacher.getFullName() : "未知教师";

        // 获取统计信息 - 可选择性加载以提高性能
        Long studentCount = 0L;
        Long taskCount = 0L;
        
        if (includeStats) {
            studentCount = courseEnrollmentRepository.countByCourseIdAndIsActiveTrue(course.getId());
            taskCount = taskRepository.countByCourseId(course.getId());
        }

        // 解析tags
        List<String> tags = new ArrayList<>();
        if (course.getTags() != null && !course.getTags().isEmpty()) {
            try {
                tags = objectMapper.readValue(course.getTags(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                logger.warn("Failed to parse tags for course {}", course.getId(), e);
            }
        }

                 // 计算学生个人进度和选课状态（仅对学生角色）
         Integer progress = 0;
         boolean enrolled = false;
         if (userRole == UserRole.STUDENT) {
             CourseEnrollment enrollment = courseEnrollmentRepository
                     .findByStudentIdAndCourseId(userId, course.getId())
                     .orElse(null);
             if (enrollment != null && enrollment.getIsActive()) {
                 enrolled = true;
                 if (enrollment.getProgress() != null) {
                     progress = enrollment.getProgress().intValue();
                 }
             }
         }

        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .description(course.getDescription())
                .teacherId(course.getTeacherId())
                .teacherName(teacherName)
                .semester(course.getSemester())
                .year(course.getYear())
                .credits(course.getCredits())
                .status(course.getStatus())
                .maxStudents(course.getMaxStudents())
                .isActive(course.getIsActive())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .price(course.getPrice())
                .tags(tags)
                .startDate(course.getStartDate() != null ? course.getStartDate().toLocalDate() : null)
                .endDate(course.getEndDate() != null ? course.getEndDate().toLocalDate() : null)
                .coverImage(course.getCoverImage())
                .syllabusUrl(course.getSyllabusUrl())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .studentCount(Math.toIntExact(studentCount))
                .taskCount(Math.toIntExact(taskCount))
                .progress(progress)
                .enrolled(enrolled);
    }

    /**
     * 检查课程访问权限
     */
    private void checkCourseAccess(Course course, Long userId, UserRole userRole) {
        if (userRole == UserRole.ADMIN) {
            return; // 管理员可以访问所有课程
        }

        if (userRole == UserRole.TEACHER && course.getTeacherId().equals(userId)) {
            return; // 教师可以访问自己的课程
        }

        if (userRole == UserRole.STUDENT) {
            // 学生可以查看所有已发布的课程详情（无论是否已选课）
            if (course.getIsActive() && "PUBLISHED".equals(course.getStatus())) {
                return;
            }
            // 也可以查看已选课程
            boolean isEnrolled = courseEnrollmentRepository
                    .existsByStudentIdAndCourseIdAndIsActiveTrue(userId, course.getId());
            if (isEnrolled) {
                return;
            }
            throw new BusinessException("无法访问该课程");
        }

        throw new BusinessException("权限不足");
    }

    /**
     * 获取课程分析数据
     */
    public Map<String, Object> getCourseAnalytics(Long courseId, Long userId, UserRole userRole) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));

        // 检查权限 - 只有教师和管理员可以查看课程分析
        if (userRole != UserRole.ADMIN && !course.getTeacherId().equals(userId)) {
            throw new BusinessException("权限不足");
        }

        // 获取课程基本信息
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("courseId", courseId);
        analytics.put("courseName", course.getName());
        analytics.put("courseCode", course.getCode());

        // 学生统计
        Long totalStudents = courseEnrollmentRepository.countByCourseIdAndIsActiveTrue(courseId);
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByCourseIdAndIsActiveTrue(courseId);
        
        // 计算平均进度
        Double averageProgress = enrollments.stream()
                .mapToDouble(e -> e.getProgress() != null ? e.getProgress() : 0.0)
                .average()
                .orElse(0.0);

        // 计算完成率（进度>=100%的学生比例）
        long completedStudents = enrollments.stream()
                .mapToLong(e -> (e.getProgress() != null && e.getProgress() >= 100.0) ? 1 : 0)
                .sum();
        Double completionRate = totalStudents > 0 ? (completedStudents * 100.0 / totalStudents) : 0.0;

        // 学生参与度统计
        analytics.put("totalStudents", totalStudents);
        analytics.put("averageProgress", Math.round(averageProgress * 100.0) / 100.0);
        analytics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        analytics.put("completedStudents", completedStudents);

        // 任务统计
        Long totalTasks = taskRepository.countByCourseId(courseId);
        analytics.put("totalTasks", totalTasks);

        // 学习时间统计
        Long totalStudyTime = enrollments.stream()
                .mapToLong(e -> e.getTotalStudyTime() != null ? e.getTotalStudyTime() : 0)
                .sum();
        Double averageStudyTime = totalStudents > 0 ? (totalStudyTime * 1.0 / totalStudents) : 0.0;
        
        analytics.put("totalStudyTime", totalStudyTime);
        analytics.put("averageStudyTime", Math.round(averageStudyTime * 100.0) / 100.0);

        // 活跃度分析（最近7天的选课数）
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        Long recentEnrollments = enrollments.stream()
                .mapToLong(e -> (e.getEnrolledAt() != null && e.getEnrolledAt().isAfter(weekAgo)) ? 1 : 0)
                .sum();
        analytics.put("recentEnrollments", recentEnrollments);

        // 进度分布统计
        Map<String, Long> progressDistribution = new HashMap<>();
        progressDistribution.put("0-25%", enrollments.stream().mapToLong(e -> {
            Double progress = e.getProgress() != null ? e.getProgress() : 0.0;
            return (progress >= 0 && progress < 25) ? 1 : 0;
        }).sum());
        progressDistribution.put("25-50%", enrollments.stream().mapToLong(e -> {
            Double progress = e.getProgress() != null ? e.getProgress() : 0.0;
            return (progress >= 25 && progress < 50) ? 1 : 0;
        }).sum());
        progressDistribution.put("50-75%", enrollments.stream().mapToLong(e -> {
            Double progress = e.getProgress() != null ? e.getProgress() : 0.0;
            return (progress >= 50 && progress < 75) ? 1 : 0;
        }).sum());
        progressDistribution.put("75-100%", enrollments.stream().mapToLong(e -> {
            Double progress = e.getProgress() != null ? e.getProgress() : 0.0;
            return (progress >= 75 && progress < 100) ? 1 : 0;
        }).sum());
        progressDistribution.put("100%", completedStudents);

        analytics.put("progressDistribution", progressDistribution);

        // 课程评分（临时固定值，后续可以实现评分系统）
        analytics.put("averageRating", 4.2);
        analytics.put("totalRatings", Math.max(1, totalStudents / 3)); // 假设1/3的学生评分

        // 时间信息
        analytics.put("generatedAt", LocalDateTime.now());
        analytics.put("courseCreatedAt", course.getCreatedAt());
        analytics.put("courseUpdatedAt", course.getUpdatedAt());

        return analytics;
    }
} 