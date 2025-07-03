package com.aicourse.controller;

import com.aicourse.dto.SubmissionCreateRequest;
import com.aicourse.dto.SubmissionGradeRequest;
import com.aicourse.entity.Submission;
import com.aicourse.entity.User;
import com.aicourse.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    // 学生提交作业（支持文件上传、多次提交）
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Submission> submit(
            @RequestParam Long taskId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        Submission submission = submissionService.submit(taskId, currentUser.getId(), content, file);
        return ResponseEntity.ok(submission);
    }

    // 学生查询自己某作业的所有提交
    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Submission>> getMySubmissions(
            @RequestParam Long taskId,
            @AuthenticationPrincipal User currentUser) {
        List<Submission> list = submissionService.getSubmissionsByStudent(taskId, currentUser.getId());
        return ResponseEntity.ok(list);
    }

    // 教师查询某作业所有学生的提交
    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Submission>> getAllSubmissions(
            @RequestParam Long taskId) {
        List<Submission> list = submissionService.getSubmissionsByTask(taskId);
        return ResponseEntity.ok(list);
    }

    // 教师批改作业
    @PutMapping("/{id}/grade")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Submission> grade(
            @PathVariable Long id,
            @RequestBody SubmissionGradeRequest request,
            @AuthenticationPrincipal User currentUser) {
        Submission submission = submissionService.gradeSubmission(id, request, currentUser.getId());
        return ResponseEntity.ok(submission);
    }
} 