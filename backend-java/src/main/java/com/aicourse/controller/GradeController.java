package com.aicourse.controller;

import com.aicourse.entity.Submission;
import com.aicourse.entity.User;
import com.aicourse.exception.BusinessException;
import com.aicourse.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grades")
@CrossOrigin(origins = "*")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getGrades(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            @AuthenticationPrincipal User currentUser) {

        try {
            Pageable pageable = PageRequest.of(skip / limit, limit);
            Page<Submission> gradePage = gradeService.getGrades(pageable, courseId, studentId, currentUser);

            // 格式化成绩数据
            List<Map<String, Object>> gradeList = gradeService.formatGradeList(gradePage.getContent());

            Map<String, Object> response = new HashMap<>();
            response.put("grades", gradeList);
            response.put("total", gradePage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("获取成绩列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/export")
    public ResponseEntity<String> exportGrades(
            @RequestBody Map<String, Object> exportData,
            @AuthenticationPrincipal User currentUser) {

        try {
            Long courseId = exportData.get("course_id") != null ? 
                Long.valueOf(exportData.get("course_id").toString()) : null;
            String format = (String) exportData.getOrDefault("format", "csv");

            if (!"csv".equals(format)) {
                throw new BusinessException("不支持的导出格式");
            }

            String csvContent = gradeService.exportGradesAsCsv(courseId, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "grades.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);

        } catch (Exception e) {
            throw new BusinessException("导出成绩失败: " + e.getMessage());
        }
    }

    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<Map<String, Object>> updateGrade(
            @PathVariable Long submissionId,
            @RequestBody Map<String, Object> gradeData,
            @AuthenticationPrincipal User currentUser) {

        try {
            gradeService.updateGrade(submissionId, gradeData, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "成绩更新成功");
            response.put("submission_id", submissionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new BusinessException("更新成绩失败: " + e.getMessage());
        }
    }

    @GetMapping("/ai-analysis")
    public ResponseEntity<Map<String, Object>> getAiGradeAnalysis(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long taskId,
            @AuthenticationPrincipal User currentUser) {

        try {
            Map<String, Object> analysis = gradeService.getAiGradeAnalysis(courseId, taskId, currentUser);
            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            throw new BusinessException("AI成绩分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getGradeStatistics(
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser) {

        try {
            Map<String, Object> statistics = gradeService.getGradeStatistics(courseId, currentUser);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            throw new BusinessException("获取成绩统计失败: " + e.getMessage());
        }
    }
} 