package com.aicourse.service;

import com.aicourse.entity.Submission;
import com.aicourse.entity.User;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.aicourse.dto.SubmissionGradeRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;

@Service
@Transactional
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    public Map<String, Object> getSubmissions(Long taskId, Long userId, int skip, int limit, User currentUser) {
        List<Submission> allSubmissions = submissionRepository.findAll();
        
        // 应用过滤条件
        List<Submission> filteredSubmissions = allSubmissions.stream()
            .filter(submission -> taskId == null || submission.getTask().getId().equals(taskId))
            .filter(submission -> userId == null || submission.getStudent().getId().equals(userId))
            .collect(Collectors.toList());
        
        // 权限检查：学生只能看到自己的提交
        if (currentUser.getRole().name().equals("STUDENT")) {
            filteredSubmissions = filteredSubmissions.stream()
                .filter(submission -> submission.getStudent().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }
        
        // 分页处理
        int start = skip;
        int end = Math.min((start + limit), filteredSubmissions.size());
        List<Submission> pageContent = filteredSubmissions.subList(start, end);
        
        // 转换为返回格式
        List<Map<String, Object>> submissionList = pageContent.stream()
            .map(this::convertSubmissionToMap)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("grades", submissionList); // 兼容前端API
        result.put("total", filteredSubmissions.size());
        
        return result;
    }

    public Map<String, Object> getSubmission(Long submissionId, User currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("提交不存在"));
        
        // 权限检查
        if (currentUser.getRole().name().equals("STUDENT") && 
            !submission.getStudent().getId().equals(currentUser.getId())) {
            throw new BusinessException("无权访问此提交");
        }
        
        return convertSubmissionToDetailMap(submission);
    }

    public void gradeSubmission(Long submissionId, Double score, String feedback, User currentUser) {
        // 权限检查：只有教师和管理员可以批改
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权批改作业");
        }
        
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("提交不存在"));
        
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(Submission.SubmissionStatus.GRADED);
        
        submissionRepository.save(submission);
    }

    public void deleteSubmission(Long submissionId, User currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("提交不存在"));
        
        // 权限检查：学生只能删除自己的提交，教师和管理员可以删除任何提交
        if (currentUser.getRole().name().equals("STUDENT") && 
            !submission.getStudent().getId().equals(currentUser.getId())) {
            throw new BusinessException("无权删除此提交");
        }
        
        submissionRepository.delete(submission);
    }

    public Map<String, Object> batchGrade(Map<String, Object> batchData, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权进行批量评分");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> submissions = (List<Map<String, Object>>) batchData.get("submissions");
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Map<String, Object> submissionData : submissions) {
            try {
                Long submissionId = Long.valueOf(submissionData.get("submissionId").toString());
                Double score = Double.valueOf(submissionData.get("score").toString());
                String feedback = (String) submissionData.get("feedback");
                
                gradeSubmission(submissionId, score, feedback, currentUser);
                successCount++;
            } catch (Exception e) {
                failureCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "批量评分完成");
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        
        return result;
    }

    public Map<String, Object> getSubmissionStatistics(Long taskId, Long courseId, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权访问统计数据");
        }
        
        List<Submission> submissions = submissionRepository.findAll();
        
        // 过滤条件
        if (taskId != null) {
            submissions = submissions.stream()
                .filter(s -> s.getTask().getId().equals(taskId))
                .collect(Collectors.toList());
        }
        
        if (courseId != null) {
            submissions = submissions.stream()
                .filter(s -> s.getTask().getCourse() != null && 
                           s.getTask().getCourse().getId().equals(courseId))
                .collect(Collectors.toList());
        }
        
        // 如果是教师，只能看到自己课程的统计
        if (currentUser.getRole().name().equals("TEACHER")) {
            submissions = submissions.stream()
                .filter(s -> s.getTask().getCreator().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }
        
        // 计算统计数据
        long totalSubmissions = submissions.size();
        long gradedSubmissions = submissions.stream()
            .filter(s -> s.getScore() != null)
            .count();
        long pendingSubmissions = totalSubmissions - gradedSubmissions;
        
        // 计算平均分
        OptionalDouble avgScore = submissions.stream()
            .filter(s -> s.getScore() != null)
            .mapToDouble(Submission::getScore)
            .average();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSubmissions", totalSubmissions);
        statistics.put("gradedSubmissions", gradedSubmissions);
        statistics.put("pendingSubmissions", pendingSubmissions);
        statistics.put("averageScore", avgScore.isPresent() ? Math.round(avgScore.getAsDouble() * 10.0) / 10.0 : 0.0);
        statistics.put("gradingRate", totalSubmissions > 0 ? Math.round((double) gradedSubmissions / totalSubmissions * 100.0) : 0.0);
        
        return statistics;
    }

    private Map<String, Object> convertSubmissionToMap(Submission submission) {
        Map<String, Object> submissionData = new HashMap<>();
        submissionData.put("id", submission.getId());
        submissionData.put("taskId", submission.getTask().getId());
        submissionData.put("userId", submission.getStudent().getId());
        submissionData.put("content", submission.getContent());
        submissionData.put("fileUrl", submission.getFileUrls());
        submissionData.put("score", submission.getScore());
        submissionData.put("feedback", submission.getFeedback());
        submissionData.put("submittedAt", submission.getSubmittedAt());
        submissionData.put("gradedAt", submission.getGradedAt());
        submissionData.put("status", submission.getStatus() != null ? submission.getStatus().name().toLowerCase() : "pending");
        submissionData.put("userName", submission.getStudent().getFullName());
        submissionData.put("taskTitle", submission.getTask().getTitle());
        
        return submissionData;
    }

    private Map<String, Object> convertSubmissionToDetailMap(Submission submission) {
        Map<String, Object> submissionData = convertSubmissionToMap(submission);
        
        // 添加详细信息
        submissionData.put("studentName", submission.getStudent().getFullName());
        submissionData.put("studentId", submission.getStudent().getStudentId());
        submissionData.put("courseName", submission.getTask().getCourse() != null ? 
                                        submission.getTask().getCourse().getName() : "未知课程");
        submissionData.put("taskName", submission.getTask().getTitle());
        submissionData.put("taskType", submission.getTask().getTaskType());
        submissionData.put("maxScore", submission.getTask().getMaxScore());
        
        return submissionData;
    }

    // 学生提交作业（支持文件上传、多次提交）
    public Submission submit(Long taskId, Long studentId, String content, MultipartFile file) {
        Submission submission = new Submission();
        submission.setTaskId(taskId);
        submission.setStudentId(studentId);
        submission.setContent(content);
        submission.setSubmittedAt(LocalDateTime.now());
        // 文件处理：保存到项目根目录uploads目录
        if (file != null && !file.isEmpty()) {
            String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads";
            File dir = new File(uploadsDir);
            if (!dir.exists()) dir.mkdirs();
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(dir, fileName);
            try {
                file.transferTo(dest);
                submission.setFileUrls(fileName);
            } catch (IOException e) {
                throw new BusinessException("文件保存失败: " + e.getMessage());
            }
        }
        // 多次提交：查找历史提交次数
        List<Submission> history = submissionRepository.findByTaskIdAndStudentId(taskId, studentId);
        submission.setAttemptNumber(history == null ? 1 : history.size() + 1);
        return submissionRepository.save(submission);
    }

    // 学生查自己提交
    public List<Submission> getSubmissionsByStudent(Long taskId, Long studentId) {
        return submissionRepository.findByTaskIdAndStudentId(taskId, studentId);
    }

    // 教师查所有提交
    public List<Submission> getSubmissionsByTask(Long taskId) {
        return submissionRepository.findByTaskId(taskId);
    }

    // 教师批改（重载，支持DTO）
    public Submission gradeSubmission(Long submissionId, SubmissionGradeRequest request, Long graderId) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("提交不存在"));
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setGraderId(graderId);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(Submission.SubmissionStatus.GRADED);
        return submissionRepository.save(submission);
    }
}