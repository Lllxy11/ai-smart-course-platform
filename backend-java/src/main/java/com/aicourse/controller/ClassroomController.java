package com.aicourse.controller;

import com.aicourse.entity.Classroom;
import com.aicourse.entity.User;
import com.aicourse.exception.BusinessException;
import com.aicourse.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classes")
@CrossOrigin(origins = "*")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getClassrooms(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(name = "grade_level", required = false) String gradeLevel,
            @RequestParam(required = false) String major,
            @AuthenticationPrincipal User currentUser) {

        try {
            Pageable pageable = PageRequest.of(skip / limit, limit);
            Page<Classroom> classroomPage = classroomService.getClassrooms(
                    pageable, search, gradeLevel, major, currentUser);

            // 格式化班级列表数据
            List<Map<String, Object>> classroomList = classroomService.formatClassroomList(
                    classroomPage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("classes", classroomList);
            response.put("total", classroomPage.getTotalElements());
            response.put("page", classroomPage.getNumber() + 1);
            response.put("page_size", limit);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("获取班级列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> getClassroom(
            @PathVariable Long classId,
            @AuthenticationPrincipal User currentUser) {

        try {
            Map<String, Object> classroomDetail = classroomService.getClassroomDetail(classId, currentUser);
            return ResponseEntity.ok(classroomDetail);

        } catch (Exception e) {
            throw new BusinessException("获取班级详情失败: " + e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createClassroom(
            @RequestBody Map<String, Object> classroomData,
            @AuthenticationPrincipal User currentUser) {

        try {
            Classroom classroom = classroomService.createClassroom(classroomData, currentUser);

            // 获取班主任姓名
            String advisorName = null;
            if (classroom.getAdvisorId() != null) {
                advisorName = "班主任姓名"; // 实际项目中应该查询数据库
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", classroom.getId());
            response.put("name", classroom.getName());
            response.put("code", classroom.getCode());
            response.put("description", classroom.getDescription());
            response.put("grade_level", classroom.getGradeLevel());
            response.put("major", classroom.getMajor());
            response.put("advisor_id", classroom.getAdvisorId());
            response.put("max_students", classroom.getMaxStudents());
            response.put("student_count", 0);
            response.put("is_active", classroom.getIsActive());
            response.put("created_at", classroom.getCreatedAt());
            response.put("updated_at", classroom.getUpdatedAt());
            response.put("advisor_name", advisorName);
            response.put("students", List.of());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("创建班级失败: " + e.getMessage());
        }
    }

    @PutMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> updateClassroom(
            @PathVariable Long classId,
            @RequestBody Map<String, Object> classroomData,
            @AuthenticationPrincipal User currentUser) {

        try {
            Classroom classroom = classroomService.updateClassroom(classId, classroomData, currentUser);

            // 格式化返回数据
            List<Map<String, Object>> formattedList = classroomService.formatClassroomList(
                    List.of(classroom));
            Map<String, Object> classroomInfo = formattedList.get(0);

            return ResponseEntity.ok(classroomInfo);

        } catch (Exception e) {
            throw new BusinessException("更新班级失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<Map<String, Object>> deleteClassroom(
            @PathVariable Long classId,
            @AuthenticationPrincipal User currentUser) {

        try {
            classroomService.deleteClassroom(classId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "班级删除成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("删除班级失败: " + e.getMessage());
        }
    }

    @PostMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> addStudentToClassroom(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal User currentUser) {

        try {
            classroomService.addStudentToClassroom(classId, studentId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "学生添加到班级成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("添加学生到班级失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> removeStudentFromClassroom(
            @PathVariable Long classId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal User currentUser) {

        try {
            classroomService.removeStudentFromClassroom(classId, studentId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "学生从班级中移除成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("从班级中移除学生失败: " + e.getMessage());
        }
    }
} 