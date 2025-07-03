package com.aicourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 学习记录实体类
 */
@Entity
@Table(name = "learning_records")
public class LearningRecord {

    public enum ActionType {
        LOGIN,              // 登录
        VIEW_COURSE,        // 查看课程
        VIEW_TASK,          // 查看任务
        START_TASK,         // 开始任务
        SUBMIT_TASK,        // 提交任务
        WATCH_VIDEO,        // 观看视频
        READ_MATERIAL,      // 阅读资料
        DOWNLOAD_FILE,      // 下载文件
        TAKE_QUIZ,          // 参加测验
        VIEW_GRADE,         // 查看成绩
        DISCUSSION,         // 参与讨论
        VIEW,               // 查看知识点
        START_LEARNING,     // 开始学习
        REVIEW,             // 复习
        PRACTICE,           // 练习
        COMPLETE            // 完成
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 关联信息
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonIgnore
    private User student;

    @Column(name = "course_id")
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    @JsonIgnore
    private Course course;

    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @JsonIgnore
    private Task task;

    @Column(name = "knowledge_point_id")
    private Long knowledgePointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_point_id", insertable = false, updatable = false)
    @JsonIgnore
    private KnowledgePoint knowledgePoint;

    // 行为记录
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "action_detail", columnDefinition = "TEXT")
    private String actionDetail; // JSON数据

    // 时间记录
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Integer duration = 0; // 持续时间(秒)

    // 学习效果
    @Column(name = "completion_rate")
    private Double completionRate = 0.0; // 完成率

    @Column(name = "interaction_count")
    private Integer interactionCount = 0; // 交互次数

    // 设备和环境
    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // 学习状态
    @Column(name = "focus_score")
    private Double focusScore; // 专注度评分

    @Column(name = "difficulty_rating")
    private Integer difficultyRating; // 难度评价

    @Column(name = "satisfaction")
    private Integer satisfaction; // 满意度

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 新增字段
    @Column(name = "learning_time")
    private LocalDateTime learningTime;

    @Column(name = "completed")
    private boolean completed = false;

    @Column(name = "score")
    private Integer score;

    // 构造函数
    public LearningRecord() {}

    public LearningRecord(Long studentId, ActionType actionType) {
        this.studentId = studentId;
        this.actionType = actionType;
        this.startTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getActionDetail() {
        return actionDetail;
    }

    public void setActionDetail(String actionDetail) {
        this.actionDetail = actionDetail;
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

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Integer getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(Integer interactionCount) {
        this.interactionCount = interactionCount;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Double getFocusScore() {
        return focusScore;
    }

    public void setFocusScore(Double focusScore) {
        this.focusScore = focusScore;
    }

    public Integer getDifficultyRating() {
        return difficultyRating;
    }

    public void setDifficultyRating(Integer difficultyRating) {
        this.difficultyRating = difficultyRating;
    }

    public Integer getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Integer satisfaction) {
        this.satisfaction = satisfaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(Long knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public KnowledgePoint getKnowledgePoint() {
        return knowledgePoint;
    }

    public void setKnowledgePoint(KnowledgePoint knowledgePoint) {
        this.knowledgePoint = knowledgePoint;
        if (knowledgePoint != null) {
            this.knowledgePointId = knowledgePoint.getId();
        }
    }

    public LocalDateTime getLearningTime() {
        return learningTime;
    }

    public void setLearningTime(LocalDateTime learningTime) {
        this.learningTime = learningTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    /**
     * 结束学习活动并计算持续时间
     */
    public void endActivity() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.duration = (int) java.time.Duration.between(this.startTime, this.endTime).getSeconds();
        }
    }

    @Override
    public String toString() {
        return "LearningRecord{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", actionType=" + actionType +
                ", duration=" + duration +
                '}';
    }
} 