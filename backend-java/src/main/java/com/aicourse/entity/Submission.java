package com.aicourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 提交实体类
 */
@Entity
@Table(name = "submissions")
public class Submission {

    public enum SubmissionStatus {
        SUBMITTED,      // 已提交
        GRADED,         // 已评分
        RETURNED,       // 已返回
        LATE            // 迟交
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 关联信息
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @JsonIgnore
    private Task task;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonIgnore
    private User student;

    // 提交内容
    @Column(columnDefinition = "TEXT")
    private String content; // 文本内容

    @Column(name = "file_urls", columnDefinition = "TEXT")
    private String fileUrls; // 文件URLs（JSON格式）

    @Column(name = "submission_data", columnDefinition = "TEXT")
    private String submissionData; // 其他提交数据（JSON格式）

    // 状态和时间
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    // 评分信息
    @Column
    private Double score; // 得分

    @Column(name = "max_score")
    private Double maxScore; // 满分

    @Column
    private Double percentage; // 百分比

    // 反馈信息
    @Column(columnDefinition = "TEXT")
    private String feedback; // 教师反馈

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback; // AI反馈

    @Column(name = "grader_id")
    private Long graderId; // 评分教师

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grader_id", insertable = false, updatable = false)
    @JsonIgnore
    private User grader;

    // 提交统计
    @Column(name = "attempt_number")
    private Integer attemptNumber = 1; // 提交次数

    @Column(name = "is_late")
    private Boolean isLate = false; // 是否迟交

    @Column(name = "late_penalty")
    private Double latePenalty = 0.0; // 迟交扣分

    // 学习数据
    @Column(name = "time_spent")
    private Integer timeSpent = 0; // 花费时间(分钟)

    @Column(name = "view_count")
    private Integer viewCount = 0; // 查看次数

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public Submission() {}

    public Submission(Long taskId, Long studentId) {
        this.taskId = taskId;
        this.studentId = studentId;
        this.submittedAt = LocalDateTime.now();
    }

    public Submission(Long taskId, Long studentId, String content) {
        this.taskId = taskId;
        this.studentId = studentId;
        this.content = content;
        this.submittedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(String fileUrls) {
        this.fileUrls = fileUrls;
    }

    public String getSubmissionData() {
        return submissionData;
    }

    public void setSubmissionData(String submissionData) {
        this.submissionData = submissionData;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public Long getGraderId() {
        return graderId;
    }

    public void setGraderId(Long graderId) {
        this.graderId = graderId;
    }

    public User getGrader() {
        return grader;
    }

    public void setGrader(User grader) {
        this.grader = grader;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public Double getLatePenalty() {
        return latePenalty;
    }

    public void setLatePenalty(Double latePenalty) {
        this.latePenalty = latePenalty;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 提交作业
     */
    public void submit() {
        this.status = SubmissionStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * 评分
     */
    public void grade(Double score, Double maxScore, String feedback, Long graderId) {
        this.score = score;
        this.maxScore = maxScore;
        this.feedback = feedback;
        this.graderId = graderId;
        this.status = SubmissionStatus.GRADED;
        this.gradedAt = LocalDateTime.now();
        
        // 计算百分比
        if (maxScore != null && maxScore > 0) {
            this.percentage = (score / maxScore) * 100;
        }
    }

    /**
     * 增加查看次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", studentId=" + studentId +
                ", status=" + status +
                ", score=" + score +
                ", attemptNumber=" + attemptNumber +
                '}';
    }
} 