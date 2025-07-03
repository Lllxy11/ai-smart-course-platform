package com.aicourse.service;

import com.aicourse.entity.*;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExamService {

    @Autowired
    private ExamPaperRepository examPaperRepository;

    public Map<String, Object> getExams(int skip, int limit, Long courseId, String status, User currentUser) {
        List<ExamPaper> allExams = examPaperRepository.findAll();
        
        // 根据用户角色过滤
        if (currentUser.getRole().name().equals("STUDENT")) {
            // 学生只能看到已发布的考试
            allExams = allExams.stream()
                .filter(exam -> "published".equals(exam.getStatus()))
                .collect(Collectors.toList());
        } else if (currentUser.getRole().name().equals("TEACHER")) {
            // 教师只能看到自己创建的考试
            allExams = allExams.stream()
                .filter(exam -> exam.getCreatedBy().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }
        
        // 应用过滤条件
        List<ExamPaper> filteredExams = allExams.stream()
            .filter(exam -> courseId == null || exam.getCourseId().equals(courseId))
            .filter(exam -> status == null || exam.getStatus().equals(status))
            .collect(Collectors.toList());
        
        // 分页处理
        int start = skip;
        int end = Math.min((start + limit), filteredExams.size());
        List<ExamPaper> pageContent = filteredExams.subList(start, end);
        
        // 转换为返回格式
        List<Map<String, Object>> examList = pageContent.stream()
            .map(exam -> convertExamToMap(exam))
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("exams", examList);
        result.put("total", filteredExams.size());
        
        return result;
    }

    public Map<String, Object> createExam(Map<String, Object> examData, User currentUser) {
        // 权限检查：只有教师和管理员可以创建考试
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权创建考试");
        }
        
        ExamPaper exam = new ExamPaper();
        exam.setTitle((String) examData.get("title"));
        exam.setDescription((String) examData.get("description"));
        
        // 设置课程
        if (examData.get("courseId") != null) {
            Long courseId = Long.valueOf(examData.get("courseId").toString());
            exam.setCourseId(courseId);
        }
        
        // 设置考试属性
        if (examData.get("duration") != null) {
            exam.setDuration(Integer.valueOf(examData.get("duration").toString()));
        } else {
            exam.setDuration(60); // 默认60分钟
        }
        
        if (examData.get("totalScore") != null) {
            exam.setTotalScore(Double.valueOf(examData.get("totalScore").toString()));
        } else {
            exam.setTotalScore(100.0); // 默认100分
        }
        
        if (examData.get("questionCount") != null) {
            exam.setQuestionCount(Integer.valueOf(examData.get("questionCount").toString()));
        }
        
        if (examData.get("instructions") != null) {
            exam.setInstructions((String) examData.get("instructions"));
        }
        
        // 设置开始和结束时间
        if (examData.get("startTime") != null) {
            exam.setStartTime(LocalDateTime.parse(examData.get("startTime").toString()));
        }
        
        if (examData.get("endTime") != null) {
            exam.setEndTime(LocalDateTime.parse(examData.get("endTime").toString()));
        }
        
        exam.setStatus("draft");
        exam.setCreatedBy(currentUser.getId());
        exam.setCreatedAt(LocalDateTime.now());
        
        exam = examPaperRepository.save(exam);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", exam.getId());
        result.put("message", "考试创建成功");
        result.put("title", exam.getTitle());
        result.put("status", exam.getStatus());
        
        return result;
    }

    public Map<String, Object> getExam(Long examId, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 权限检查
        if (currentUser.getRole().name().equals("STUDENT") && !"published".equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        } else if (currentUser.getRole().name().equals("TEACHER") && 
                   !exam.getCreatedBy().equals(currentUser.getId())) {
            throw new BusinessException("无权访问此考试");
        }
        
        return convertExamToDetailMap(exam);
    }

    public void updateExam(Long examId, Map<String, Object> examData, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 权限检查
        if (currentUser.getRole().name().equals("TEACHER") && 
            !exam.getCreatedBy().equals(currentUser.getId())) {
            throw new BusinessException("无权修改此考试");
        } else if (!currentUser.getRole().name().equals("TEACHER") && 
                   !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权修改考试");
        }
        
        // 更新考试信息
        if (examData.containsKey("title")) {
            exam.setTitle((String) examData.get("title"));
        }
        if (examData.containsKey("description")) {
            exam.setDescription((String) examData.get("description"));
        }
        if (examData.containsKey("duration")) {
            exam.setDuration(Integer.valueOf(examData.get("duration").toString()));
        }
        if (examData.containsKey("totalScore")) {
            exam.setTotalScore(Double.valueOf(examData.get("totalScore").toString()));
        }
        if (examData.containsKey("instructions")) {
            exam.setInstructions((String) examData.get("instructions"));
        }
        if (examData.containsKey("startTime")) {
            exam.setStartTime(LocalDateTime.parse(examData.get("startTime").toString()));
        }
        if (examData.containsKey("endTime")) {
            exam.setEndTime(LocalDateTime.parse(examData.get("endTime").toString()));
        }
        
        exam.setUpdatedAt(LocalDateTime.now());
        examPaperRepository.save(exam);
    }

    public void deleteExam(Long examId, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 权限检查
        if (currentUser.getRole().name().equals("TEACHER") && 
            !exam.getCreatedBy().equals(currentUser.getId())) {
            throw new BusinessException("无权删除此考试");
        } else if (!currentUser.getRole().name().equals("TEACHER") && 
                   !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权删除考试");
        }
        
        // 检查是否有学生已经参加考试 - 简化实现
        // 在实际项目中，这里应该检查ExamSession表
        
        examPaperRepository.delete(exam);
    }

    public Map<String, Object> getExamQuestions(Long examId, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 权限检查
        if (currentUser.getRole().name().equals("STUDENT") && !"published".equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        }
        
        // 获取考试题目 - 简化实现
        List<Map<String, Object>> questions = new ArrayList<>();
        
        Map<String, Object> result = new HashMap<>();
        result.put("examId", examId);
        result.put("questions", questions);
        result.put("totalQuestions", questions.size());
        result.put("totalScore", exam.getTotalScore());
        
        return result;
    }

    public void publishExam(Long examId, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 权限检查
        if (currentUser.getRole().name().equals("TEACHER") && 
            !exam.getCreatedBy().equals(currentUser.getId())) {
            throw new BusinessException("无权发布此考试");
        } else if (!currentUser.getRole().name().equals("TEACHER") && 
                   !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权发布考试");
        }
        
        // 检查考试是否有题目 - 简化实现
        // 在实际项目中，这里应该检查Question表
        
        exam.setStatus("published");
        exam.setUpdatedAt(LocalDateTime.now());
        examPaperRepository.save(exam);
    }

    public Map<String, Object> getExamStatistics(User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权访问统计数据");
        }
        
        List<ExamPaper> exams = examPaperRepository.findAll();
        
        // 如果是教师，只统计自己的考试
        if (currentUser.getRole().name().equals("TEACHER")) {
            exams = exams.stream()
                .filter(exam -> exam.getCreatedBy().equals(currentUser.getId()))
                .collect(Collectors.toList());
        }
        
        // 基础统计
        long totalExams = exams.size();
        long publishedExams = exams.stream()
            .filter(exam -> "published".equals(exam.getStatus()))
            .count();
        long draftExams = exams.stream()
            .filter(exam -> "draft".equals(exam.getStatus()))
            .count();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalExams", totalExams);
        statistics.put("publishedExams", publishedExams);
        statistics.put("draftExams", draftExams);
        statistics.put("avgScore", 0); // 暂时设为0，需要实现提交系统后计算
        statistics.put("completionRate", 0); // 暂时设为0
        
        return statistics;
    }

    public Map<String, Object> startExam(Long examId, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 检查考试状态
        if (!"published".equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        }
        
        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束");
        }
        
        // 创建考试会话 - 简化实现
        Map<String, Object> result = new HashMap<>();
        result.put("examId", examId);
        result.put("sessionId", UUID.randomUUID().toString());
        result.put("startTime", now.toString());
        result.put("duration", exam.getDuration());
        result.put("message", "考试开始");
        
        return result;
    }

    public Map<String, Object> submitExam(Long examId, Map<String, Object> answers, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 检查考试状态和时间
        if (!"published".equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        }
        
        // 保存考试答案 - 简化实现
        // 在实际项目中，这里应该保存到ExamSession表
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "考试提交成功");
        result.put("examId", examId);
        result.put("submittedAt", LocalDateTime.now().toString());
        result.put("score", 0); // 需要实现自动评分逻辑
        
        return result;
    }

    public Map<String, Object> getExamResults(Long examId, User currentUser) {
        ExamPaper exam = examPaperRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("考试不存在"));
        
        // 权限检查
        if (currentUser.getRole().name().equals("TEACHER") && 
            !exam.getCreatedBy().equals(currentUser.getId())) {
            throw new BusinessException("无权查看此考试结果");
        } else if (!currentUser.getRole().name().equals("TEACHER") && 
                   !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权查看考试结果");
        }
        
        // 获取考试结果 - 简化实现
        Map<String, Object> result = new HashMap<>();
        result.put("examId", examId);
        result.put("examTitle", exam.getTitle());
        result.put("results", new ArrayList<>());
        result.put("totalParticipants", 0);
        result.put("averageScore", 0.0);
        
        return result;
    }

    private Map<String, Object> convertExamToMap(ExamPaper exam) {
        Map<String, Object> examData = new HashMap<>();
        examData.put("id", exam.getId());
        examData.put("title", exam.getTitle());
        examData.put("description", exam.getDescription());
        examData.put("courseName", ""); // 需要关联Course表获取
        examData.put("questionCount", exam.getQuestionCount() != null ? exam.getQuestionCount() : 0);
        examData.put("totalScore", exam.getTotalScore() != null ? exam.getTotalScore() : 0);
        examData.put("duration", exam.getDuration() != null ? exam.getDuration() : 60);
        examData.put("status", exam.getStatus());
        examData.put("startTime", exam.getStartTime() != null ? exam.getStartTime().toString() : null);
        examData.put("endTime", exam.getEndTime() != null ? exam.getEndTime().toString() : null);
        examData.put("createdAt", exam.getCreatedAt() != null ? exam.getCreatedAt().toString() : null);
        
        return examData;
    }

    private Map<String, Object> convertExamToDetailMap(ExamPaper exam) {
        Map<String, Object> examData = convertExamToMap(exam);
        
        // 添加详细信息
        examData.put("instructions", exam.getInstructions());
        examData.put("courseId", exam.getCourseId());
        examData.put("createdBy", exam.getCreatedBy());
        examData.put("updatedAt", exam.getUpdatedAt() != null ? exam.getUpdatedAt().toString() : null);
        
        return examData;
    }
} 