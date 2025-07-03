package com.aicourse.service;

import com.aicourse.entity.Classroom;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.ClassroomRepository;
import com.aicourse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<Classroom> getClassrooms(Pageable pageable, String search, String gradeLevel, 
                                        String major, User currentUser) {
        
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        Specification<Classroom> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只查询活跃的班级
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));

            // 搜索过滤
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("major")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            // 年级过滤
            if (gradeLevel != null && !gradeLevel.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("gradeLevel"), gradeLevel));
            }

            // 专业过滤
            if (major != null && !major.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("major"), major));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return classroomRepository.findAll(spec, pageable);
    }

    public Map<String, Object> getClassroomDetail(Long classId, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("班级不存在"));

        // 获取班级学生
        List<User> students = userRepository.findByClassIdAndRoleAndIsActiveTrue(
                classId, UserRole.STUDENT);

        // 获取班主任信息
        String advisorName = null;
        if (classroom.getAdvisorId() != null) {
            User advisor = userRepository.findById(classroom.getAdvisorId()).orElse(null);
            if (advisor != null) {
                advisorName = advisor.getFullName();
            }
        }

        // 格式化学生数据
        List<Map<String, Object>> studentList = students.stream().map(student -> {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("id", student.getId());
            studentData.put("username", student.getUsername());
            studentData.put("full_name", student.getFullName());
            studentData.put("email", student.getEmail());
            studentData.put("student_id", student.getStudentId());
            studentData.put("enrolled_at", student.getCreatedAt());
            return studentData;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("id", classroom.getId());
        result.put("name", classroom.getName());
        result.put("code", classroom.getCode());
        result.put("description", classroom.getDescription());
        result.put("grade_level", classroom.getGradeLevel());
        result.put("major", classroom.getMajor());
        result.put("advisor_id", classroom.getAdvisorId());
        result.put("max_students", classroom.getMaxStudents());
        result.put("student_count", students.size());
        result.put("is_active", classroom.getIsActive());
        result.put("created_at", classroom.getCreatedAt());
        result.put("updated_at", classroom.getUpdatedAt());
        result.put("advisor_name", advisorName);
        result.put("students", studentList);

        return result;
    }

    public Classroom createClassroom(Map<String, Object> classroomData, User currentUser) {
        // 权限检查：只有管理员可以创建班级
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        String code = (String) classroomData.get("code");
        
        // 检查班级代码是否已存在
        if (code != null && classroomRepository.existsByCode(code)) {
            throw new BusinessException("班级代码已存在");
        }

        Long advisorId = null;
        if (classroomData.get("advisor_id") != null) {
            advisorId = Long.valueOf(classroomData.get("advisor_id").toString());
            
            // 检查班主任是否存在且为教师
            User advisor = userRepository.findById(advisorId).orElse(null);
            if (advisor == null || !advisor.getRole().equals(UserRole.TEACHER)) {
                throw new BusinessException("指定的班主任不存在或不是教师");
            }
        }

        Classroom classroom = new Classroom();
        classroom.setName((String) classroomData.get("name"));
        classroom.setCode(code);
        classroom.setDescription((String) classroomData.get("description"));
        classroom.setGradeLevel((String) classroomData.get("grade_level"));
        classroom.setMajor((String) classroomData.get("major"));
        classroom.setAdvisorId(advisorId);
        classroom.setMaxStudents((Integer) classroomData.getOrDefault("max_students", 50));
        classroom.setStudentCount(0);
        classroom.setIsActive(true);

        return classroomRepository.save(classroom);
    }

    public Classroom updateClassroom(Long classId, Map<String, Object> classroomData, User currentUser) {
        // 权限检查：只有管理员可以更新班级
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("班级不存在"));

        // 检查班级代码是否被其他班级使用
        String newCode = (String) classroomData.get("code");
        if (newCode != null && !newCode.equals(classroom.getCode()) && 
            classroomRepository.existsByCodeAndIdNot(newCode, classId)) {
            throw new BusinessException("班级代码已被其他班级使用");
        }

        // 检查班主任
        if (classroomData.get("advisor_id") != null) {
            Long advisorId = Long.valueOf(classroomData.get("advisor_id").toString());
            User advisor = userRepository.findById(advisorId).orElse(null);
            if (advisor == null || !advisor.getRole().equals(UserRole.TEACHER)) {
                throw new BusinessException("指定的班主任不存在或不是教师");
            }
            classroom.setAdvisorId(advisorId);
        }

        // 更新班级信息
        if (classroomData.get("name") != null) {
            classroom.setName((String) classroomData.get("name"));
        }
        if (newCode != null) {
            classroom.setCode(newCode);
        }
        if (classroomData.get("description") != null) {
            classroom.setDescription((String) classroomData.get("description"));
        }
        if (classroomData.get("grade_level") != null) {
            classroom.setGradeLevel((String) classroomData.get("grade_level"));
        }
        if (classroomData.get("major") != null) {
            classroom.setMajor((String) classroomData.get("major"));
        }
        if (classroomData.get("max_students") != null) {
            classroom.setMaxStudents((Integer) classroomData.get("max_students"));
        }
        if (classroomData.get("is_active") != null) {
            classroom.setIsActive((Boolean) classroomData.get("is_active"));
        }

        classroom.setUpdatedAt(LocalDateTime.now());
        return classroomRepository.save(classroom);
    }

    public void deleteClassroom(Long classId, User currentUser) {
        // 权限检查：只有管理员可以删除班级
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("班级不存在"));

        // 检查班级是否有学生
        long studentCount = userRepository.countByClassIdAndRoleAndIsActiveTrue(
                classId, UserRole.STUDENT);

        if (studentCount > 0) {
            throw new BusinessException("班级中还有学生，无法删除");
        }

        // 软删除（设置为不活跃）
        classroom.setIsActive(false);
        classroom.setUpdatedAt(LocalDateTime.now());
        classroomRepository.save(classroom);
    }

    public void addStudentToClassroom(Long classId, Long studentId, User currentUser) {
        // 权限检查：只有管理员可以操作
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("班级不存在"));

        User student = userRepository.findById(studentId).orElse(null);
        if (student == null || !student.getRole().equals(UserRole.STUDENT)) {
            throw new BusinessException("学生不存在");
        }

        // 检查学生是否已在其他班级
        if (student.getClassId() != null && !student.getClassId().equals(classId)) {
            throw new BusinessException("学生已在其他班级中");
        }

        // 检查班级人数是否已满
        long currentStudentCount = userRepository.countByClassIdAndRoleAndIsActiveTrue(
                classId, UserRole.STUDENT);

        if (currentStudentCount >= classroom.getMaxStudents()) {
            throw new BusinessException("班级人数已满");
        }

        // 将学生添加到班级
        student.setClassId(classId);
        student.setClassName(classroom.getName());
        userRepository.save(student);

        // 更新班级学生数量
        classroom.setStudentCount((int) (currentStudentCount + 1));
        classroomRepository.save(classroom);
    }

    public void removeStudentFromClassroom(Long classId, Long studentId, User currentUser) {
        // 权限检查：只有管理员可以操作
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("权限不足");
        }

        User student = userRepository.findByIdAndClassIdAndRole(studentId, classId, UserRole.STUDENT);
        if (student == null) {
            throw new BusinessException("学生不在指定班级中");
        }

        // 将学生从班级中移除
        student.setClassId(null);
        student.setClassName(null);
        userRepository.save(student);

        // 更新班级学生数量
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("班级不存在"));
        
        long currentStudentCount = userRepository.countByClassIdAndRoleAndIsActiveTrue(
                classId, UserRole.STUDENT);
        
        classroom.setStudentCount((int) currentStudentCount);
        classroomRepository.save(classroom);
    }

    public List<Map<String, Object>> formatClassroomList(List<Classroom> classrooms) {
        return classrooms.stream().map(classroom -> {
            // 获取实际学生数量
            long studentCount = userRepository.countByClassIdAndRoleAndIsActiveTrue(
                    classroom.getId(), UserRole.STUDENT);

            // 获取班主任姓名
            String advisorName = null;
            if (classroom.getAdvisorId() != null) {
                User advisor = userRepository.findById(classroom.getAdvisorId()).orElse(null);
                if (advisor != null) {
                    advisorName = advisor.getFullName();
                }
            }

            Map<String, Object> classData = new HashMap<>();
            classData.put("id", classroom.getId());
            classData.put("name", classroom.getName());
            classData.put("code", classroom.getCode());
            classData.put("description", classroom.getDescription());
            classData.put("grade_level", classroom.getGradeLevel());
            classData.put("major", classroom.getMajor());
            classData.put("advisor_id", classroom.getAdvisorId());
            classData.put("max_students", classroom.getMaxStudents());
            classData.put("student_count", (int) studentCount);
            classData.put("is_active", classroom.getIsActive());
            classData.put("created_at", classroom.getCreatedAt());
            classData.put("updated_at", classroom.getUpdatedAt());
            classData.put("advisor_name", advisorName);

            return classData;
        }).collect(Collectors.toList());
    }
} 