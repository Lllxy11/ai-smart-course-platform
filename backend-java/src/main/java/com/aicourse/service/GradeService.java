package com.aicourse.service;

import com.aicourse.entity.Course;
import com.aicourse.entity.Submission;
import com.aicourse.entity.Task;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.CourseRepository;
import com.aicourse.repository.SubmissionRepository;
import com.aicourse.repository.TaskRepository;
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
public class GradeService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<Submission> getGrades(Pageable pageable, Long courseId, Long studentId, User currentUser) {
        
        Specification<Submission> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 只查询已评分的提交
            predicates.add(criteriaBuilder.isNotNull(root.get("score")));

            // 课程过滤
            if (courseId != null) {
                predicates.add(criteriaBuilder.equal(root.get("task").get("courseId"), courseId));
            }

            // 学生过滤
            if (studentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("studentId"), studentId));
            }

            // 权限过滤：学生只能看到自己的成绩
            if (currentUser.getRole().equals(UserRole.STUDENT)) {
                predicates.add(criteriaBuilder.equal(root.get("studentId"), currentUser.getId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return submissionRepository.findAll(spec, pageable);
    }

    public List<Map<String, Object>> formatGradeList(List<Submission> submissions) {
        return submissions.stream().map(submission -> {
            Map<String, Object> gradeData = new HashMap<>();
            
            // 获取学生信息
            User student = userRepository.findById(submission.getStudentId()).orElse(null);
            String studentName = student != null ? student.getFullName() : "未知学生";
            String studentIdStr = student != null ? student.getStudentId() : "";

            // 获取任务和课程信息
            Task task = taskRepository.findById(submission.getTaskId()).orElse(null);
            String taskName = task != null ? task.getTitle() : "未知任务";
            String taskType = task != null ? task.getTaskType().toString() : "assignment";
            Double maxScore = task != null ? task.getMaxScore() : 100.0;

            String courseName = "未知课程";
            if (task != null && task.getCourseId() != null) {
                Course course = courseRepository.findById(task.getCourseId()).orElse(null);
                courseName = course != null ? course.getName() : "未知课程";
            }

            gradeData.put("id", submission.getId());
            gradeData.put("student_name", studentName);
            gradeData.put("student_id", studentIdStr);
            gradeData.put("course_name", courseName);
            gradeData.put("task_name", taskName);
            gradeData.put("task_type", taskType);
            gradeData.put("score", submission.getScore() != null ? submission.getScore() : 0.0);
            gradeData.put("max_score", maxScore);
            gradeData.put("submitted_at", submission.getSubmittedAt());
            gradeData.put("graded_at", submission.getGradedAt());
            gradeData.put("feedback", submission.getFeedback() != null ? submission.getFeedback() : "");

            return gradeData;
        }).collect(Collectors.toList());
    }

    public String exportGradesAsCsv(Long courseId, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权导出成绩");
        }

        List<Submission> submissions = submissionRepository.findGradedSubmissionsByCourse(courseId);

        StringBuilder csv = new StringBuilder();
        // CSV表头
        csv.append("学生姓名,学号,课程名称,任务名称,任务类型,得分,满分,提交时间,批改时间,反馈\n");

        for (Submission submission : submissions) {
            try {
                User student = userRepository.findById(submission.getStudentId()).orElse(null);
                Task task = taskRepository.findById(submission.getTaskId()).orElse(null);
                Course course = null;
                if (task != null && task.getCourseId() != null) {
                    course = courseRepository.findById(task.getCourseId()).orElse(null);
                }

                csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    student != null ? student.getFullName() : "未知学生",
                    student != null ? student.getStudentId() : "",
                    course != null ? course.getName() : "未知课程",
                    task != null ? task.getTitle() : "未知任务",
                    task != null ? task.getTaskType().toString() : "assignment",
                    submission.getScore() != null ? submission.getScore() : 0.0,
                    task != null ? task.getMaxScore() : 0.0,
                    submission.getSubmittedAt() != null ? submission.getSubmittedAt().toString() : "",
                    submission.getGradedAt() != null ? submission.getGradedAt().toString() : "",
                    submission.getFeedback() != null ? submission.getFeedback().replace(",", ";") : ""
                ));
            } catch (Exception e) {
                // 跳过有问题的记录
                continue;
            }
        }

        return csv.toString();
    }

    public Submission updateGrade(Long submissionId, Map<String, Object> gradeData, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权修改成绩");
        }

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("提交记录不存在"));

        if (gradeData.get("score") != null) {
            Double score = Double.valueOf(gradeData.get("score").toString());
            submission.setScore(score);
        }
        if (gradeData.get("feedback") != null) {
            submission.setFeedback((String) gradeData.get("feedback"));
        }

        submission.setGradedAt(LocalDateTime.now());
        return submissionRepository.save(submission);
    }

    public Map<String, Object> getGradeStatistics(Long courseId, User currentUser) {
        try {
            List<Submission> submissions;

            if (currentUser.getRole().equals(UserRole.STUDENT)) {
                // 学生只能看到自己的统计
                submissions = submissionRepository.findGradedSubmissionsByStudentAndCourse(
                        currentUser.getId(), courseId);
            } else {
                // 教师和管理员可以看到课程统计
                submissions = submissionRepository.findGradedSubmissionsByCourse(courseId);
            }

            if (submissions.isEmpty()) {
                return createEmptyStatistics();
            }

            // 计算统计数据
            List<Double> scores = submissions.stream()
                    .map(s -> s.getScore() != null ? s.getScore() : 0.0)
                    .collect(Collectors.toList());

            double averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double highestScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

            // 计算及格率（假设60%为及格线）
            long passCount = submissions.stream()
                    .filter(s -> {
                        if (s.getScore() == null) return false;
                        Task task = taskRepository.findById(s.getTaskId()).orElse(null);
                        if (task == null || task.getMaxScore() == null) return false;
                        return s.getScore() / task.getMaxScore() >= 0.6;
                    })
                    .count();

            double passRate = scores.isEmpty() ? 0.0 : (double) passCount / scores.size() * 100;

            // 找出最高分学生
            String topStudent = "";
            Optional<Submission> topSubmission = submissions.stream()
                    .max(Comparator.comparing(s -> s.getScore() != null ? s.getScore() : 0.0));
            
            if (topSubmission.isPresent()) {
                User student = userRepository.findById(topSubmission.get().getStudentId()).orElse(null);
                topStudent = student != null ? student.getFullName() : "未知学生";
            }

            // 计算提交率
            long totalTasks = courseId != null ? 
                taskRepository.countByCourseId(courseId) : 
                taskRepository.count();

            double submissionRate = totalTasks > 0 ? 
                (double) submissions.size() / totalTasks * 100 : 0.0;

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("average_score", Math.round(averageScore * 10.0) / 10.0);
            statistics.put("highest_score", highestScore);
            statistics.put("submission_rate", Math.round(submissionRate * 10.0) / 10.0);
            statistics.put("pass_rate", Math.round(passRate * 10.0) / 10.0);
            statistics.put("top_student", topStudent);
            statistics.put("submitted_count", submissions.size());
            statistics.put("total_count", (int) totalTasks);
            statistics.put("pass_count", (int) passCount);

            return statistics;

        } catch (Exception e) {
            return createEmptyStatistics();
        }
    }

    public Map<String, Object> getAiGradeAnalysis(Long courseId, Long taskId, User currentUser) {
        // 权限检查
        if (!currentUser.getRole().name().equals("TEACHER") && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无权使用AI分析功能");
        }

        try {
            // 获取成绩数据
            List<Submission> submissions;
            if (taskId != null) {
                submissions = submissionRepository.findGradedSubmissionsByTask(taskId);
            } else if (courseId != null) {
                submissions = submissionRepository.findGradedSubmissionsByCourse(courseId);
            } else {
                submissions = submissionRepository.findGradedSubmissions();
            }

            if (submissions.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "没有找到相关成绩数据"
                );
            }

            // 计算基础统计
            List<Double> scores = submissions.stream()
                    .map(s -> s.getScore() != null ? s.getScore() : 0.0)
                    .collect(Collectors.toList());

            double averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double highestScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double lowestScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            
            long passCount = scores.stream().filter(s -> s >= 60.0).count();
            double passRate = (double) passCount / scores.size() * 100;

            // 成绩分布
            Map<String, Integer> gradeDistribution = new HashMap<>();
            gradeDistribution.put("90-100", (int) scores.stream().filter(s -> s >= 90.0).count());
            gradeDistribution.put("80-89", (int) scores.stream().filter(s -> s >= 80.0 && s < 90.0).count());
            gradeDistribution.put("70-79", (int) scores.stream().filter(s -> s >= 70.0 && s < 80.0).count());
            gradeDistribution.put("60-69", (int) scores.stream().filter(s -> s >= 60.0 && s < 70.0).count());
            gradeDistribution.put("0-59", (int) scores.stream().filter(s -> s < 60.0).count());

            // 调用AI分析（简化实现）
            Map<String, Object> aiAnalysis = generateBasicAnalysis(
                    averageScore, passRate, highestScore, lowestScore, scores.size());

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total_students", scores.size());
            statistics.put("average_score", Math.round(averageScore * 10.0) / 10.0);
            statistics.put("highest_score", highestScore);
            statistics.put("lowest_score", lowestScore);
            statistics.put("pass_rate", Math.round(passRate * 10.0) / 10.0);
            statistics.put("grade_distribution", gradeDistribution);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("statistics", statistics);
            result.put("ai_analysis", aiAnalysis);
            result.put("course_id", courseId);
            result.put("task_id", taskId);

            return result;

        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }

    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("average_score", 0.0);
        stats.put("highest_score", 0.0);
        stats.put("submission_rate", 0.0);
        stats.put("pass_rate", 0.0);
        stats.put("top_student", "");
        stats.put("submitted_count", 0);
        stats.put("total_count", 0);
        stats.put("pass_count", 0);
        return stats;
    }

    private Map<String, Object> generateBasicAnalysis(double avgScore, double passRate, 
                                                      double highScore, double lowScore, int totalStudents) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 整体评价
        String overallAssessment;
        if (avgScore >= 85) {
            overallAssessment = "班级平均分" + String.format("%.1f", avgScore) + "分，整体表现优秀";
        } else if (avgScore >= 75) {
            overallAssessment = "班级平均分" + String.format("%.1f", avgScore) + "分，整体表现良好";
        } else if (avgScore >= 65) {
            overallAssessment = "班级平均分" + String.format("%.1f", avgScore) + "分，整体表现一般";
        } else {
            overallAssessment = "班级平均分" + String.format("%.1f", avgScore) + "分，需要改进";
        }

        String gradeLevel = avgScore >= 75 ? "良好" : "一般";
        
        List<String> improvementSuggestions = Arrays.asList(
            "加强基础知识讲解",
            "增加课堂互动",
            "提供个性化辅导"
        );

        List<String> keyInsights = Arrays.asList(
            "需要关注成绩分化情况",
            "建议针对性教学"
        );

        List<String> riskFactors = new ArrayList<>();
        if (passRate < 80) {
            riskFactors.add("部分学生成绩偏低");
        }
        if (highScore - lowScore > 40) {
            riskFactors.add("成绩分化明显");
        }

        analysis.put("overall_assessment", overallAssessment);
        analysis.put("distribution_analysis", "成绩呈现一定的分化");
        analysis.put("teaching_effectiveness", "整体教学效果需要进一步评估");
        analysis.put("student_diagnosis", "建议关注成绩较低的学生");
        analysis.put("improvement_suggestions", improvementSuggestions);
        analysis.put("grade_level", gradeLevel);
        analysis.put("key_insights", keyInsights);
        analysis.put("risk_factors", riskFactors);

        return analysis;
    }
} 