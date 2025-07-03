package com.aicourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试试卷实体类
 */
@Entity
@Table(name = "exam_papers")
public class ExamPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 关联信息
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    @JsonIgnore
    private Course course;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    @JsonIgnore
    private User creator;

    // 考试状态和时间
    @Column(name = "status")
    private String status = "draft"; // 状态：draft, published, closed

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions; // 考试说明

    @Column(name = "start_time")
    private LocalDateTime startTime; // 考试开始时间

    @Column(name = "end_time")
    private LocalDateTime endTime; // 考试结束时间

    // 试卷配置
    @Column(name = "total_score")
    private Double totalScore = 100.0; // 总分

    @Column(name = "duration")
    private Integer duration; // 考试时长(分钟)

    @Column(name = "question_count")
    private Integer questionCount = 0; // 题目数量

    // 试卷设置
    @Column(name = "shuffle_questions")
    private Boolean shuffleQuestions = false; // 打乱题目顺序

    @Column(name = "shuffle_options")
    private Boolean shuffleOptions = false; // 打乱选项顺序

    @Column(name = "show_score")
    private Boolean showScore = true; // 显示分数

    @Column(name = "show_correct_answer")
    private Boolean showCorrectAnswer = true; // 显示正确答案

    // 考试规则
    @Column(name = "max_attempts")
    private Integer maxAttempts = 1; // 最大尝试次数

    @Column(name = "time_limit")
    private Boolean timeLimit = false; // 是否限时

    @Column(name = "allow_review")
    private Boolean allowReview = true; // 允许回顾

    // 生成规则(用于随机组卷)
    @Column(name = "generation_rules", columnDefinition = "TEXT")
    private String generationRules; // 组卷规则（JSON格式）

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 关系
    @OneToMany(mappedBy = "examPaper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ExamQuestion> questions;

    @OneToMany(mappedBy = "examPaper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ExamSession> examSessions;

    // 构造函数
    public ExamPaper() {}

    public ExamPaper(String title, Long courseId, Long creatorId) {
        this.title = title;
        this.courseId = courseId;
        this.creatorId = creatorId;
    }

    public ExamPaper(String title, String description, Long courseId, Long creatorId, Integer duration) {
        this.title = title;
        this.description = description;
        this.courseId = courseId;
        this.creatorId = creatorId;
        this.duration = duration;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    // 新增的getter/setter方法
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getCreatedBy() {
        return creatorId;
    }

    public void setCreatedBy(Long createdBy) {
        this.creatorId = createdBy;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Boolean getShuffleQuestions() {
        return shuffleQuestions;
    }

    public void setShuffleQuestions(Boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }

    public Boolean getShuffleOptions() {
        return shuffleOptions;
    }

    public void setShuffleOptions(Boolean shuffleOptions) {
        this.shuffleOptions = shuffleOptions;
    }

    public Boolean getShowScore() {
        return showScore;
    }

    public void setShowScore(Boolean showScore) {
        this.showScore = showScore;
    }

    public Boolean getShowCorrectAnswer() {
        return showCorrectAnswer;
    }

    public void setShowCorrectAnswer(Boolean showCorrectAnswer) {
        this.showCorrectAnswer = showCorrectAnswer;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Boolean getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Boolean timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Boolean getAllowReview() {
        return allowReview;
    }

    public void setAllowReview(Boolean allowReview) {
        this.allowReview = allowReview;
    }

    public String getGenerationRules() {
        return generationRules;
    }

    public void setGenerationRules(String generationRules) {
        this.generationRules = generationRules;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public List<ExamQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ExamQuestion> questions) {
        this.questions = questions;
    }

    public List<ExamSession> getExamSessions() {
        return examSessions;
    }

    public void setExamSessions(List<ExamSession> examSessions) {
        this.examSessions = examSessions;
    }

    /**
     * 更新题目数量和总分
     */
    public void updateQuestionStats() {
        if (questions != null) {
            this.questionCount = questions.size();
            this.totalScore = questions.stream()
                    .mapToDouble(ExamQuestion::getPoints)
                    .sum();
        }
    }

    @Override
    public String toString() {
        return "ExamPaper{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", questionCount=" + questionCount +
                ", totalScore=" + totalScore +
                ", duration=" + duration +
                '}';
    }
} 