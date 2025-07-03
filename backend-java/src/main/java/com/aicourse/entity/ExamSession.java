package com.aicourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 考试会话实体类
 */
@Entity
@Table(name = "exam_sessions")
public class ExamSession {

    public enum ExamStatus {
        STARTED,        // 已开始
        SUBMITTED,      // 已提交
        GRADED          // 已评分
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 关联信息
    @Column(name = "exam_paper_id", nullable = false)
    private Long examPaperId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_paper_id", insertable = false, updatable = false)
    @JsonIgnore
    private ExamPaper examPaper;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonIgnore
    private User student;

    // 考试状态
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status = ExamStatus.STARTED;

    @Column(name = "attempt_number")
    private Integer attemptNumber = 1; // 尝试次数

    // 时间记录
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "time_spent")
    private Integer timeSpent = 0; // 花费时间(秒)

    // 成绩记录
    @Column(name = "total_score")
    private Double totalScore; // 总分

    @Column(name = "percentage")
    private Double percentage; // 百分比

    // 答题记录
    @Column(name = "answers", columnDefinition = "TEXT")
    private String answers; // 学生答案（JSON格式）

    @Column(name = "question_scores", columnDefinition = "TEXT")
    private String questionScores; // 各题得分（JSON格式）

    // 考试环境
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public ExamSession() {}

    public ExamSession(Long examPaperId, Long studentId) {
        this.examPaperId = examPaperId;
        this.studentId = studentId;
        this.startTime = LocalDateTime.now();
    }

    public ExamSession(Long examPaperId, Long studentId, Integer attemptNumber) {
        this.examPaperId = examPaperId;
        this.studentId = studentId;
        this.attemptNumber = attemptNumber;
        this.startTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(Long examPaperId) {
        this.examPaperId = examPaperId;
    }

    public ExamPaper getExamPaper() {
        return examPaper;
    }

    public void setExamPaper(ExamPaper examPaper) {
        this.examPaper = examPaper;
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

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Integer timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getQuestionScores() {
        return questionScores;
    }

    public void setQuestionScores(String questionScores) {
        this.questionScores = questionScores;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
     * 提交考试
     */
    public void submit() {
        this.status = ExamStatus.SUBMITTED;
        this.submitTime = LocalDateTime.now();
        
        // 计算考试时长
        if (this.startTime != null) {
            this.timeSpent = (int) java.time.Duration.between(this.startTime, this.submitTime).getSeconds();
        }
    }

    /**
     * 评分完成
     */
    public void grade(Double totalScore, Double maxScore) {
        this.totalScore = totalScore;
        this.status = ExamStatus.GRADED;
        
        // 计算百分比
        if (maxScore != null && maxScore > 0) {
            this.percentage = (totalScore / maxScore) * 100;
        }
    }

    @Override
    public String toString() {
        return "ExamSession{" +
                "id=" + id +
                ", examPaperId=" + examPaperId +
                ", studentId=" + studentId +
                ", status=" + status +
                ", totalScore=" + totalScore +
                ", attemptNumber=" + attemptNumber +
                '}';
    }
} 