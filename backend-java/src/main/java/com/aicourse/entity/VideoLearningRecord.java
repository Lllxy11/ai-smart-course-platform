package com.aicourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频学习记录实体类
 */
@Entity
@Table(name = "video_learning_records")
public class VideoLearningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonIgnore
    private User student;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @JsonIgnore
    private Task task;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Column(name = "video_duration", nullable = false)
    private Double videoDuration; // 视频总时长(秒)

    // 观看进度记录
    @Column(name = "total_watch_time")
    private Double totalWatchTime = 0.0; // 总观看时长(秒)

    @Column(name = "actual_watch_time")
    private Double actualWatchTime = 0.0; // 实际观看时长(去重)

    @Column(name = "watch_progress")
    private Double watchProgress = 0.0; // 观看进度百分比

    @Column(name = "completion_rate")
    private Double completionRate = 0.0; // 完成率

    // 观看行为数据（存储为JSON字符串）
    @Column(name = "watch_segments", columnDefinition = "TEXT")
    private String watchSegments; // 观看片段记录

    @Column(name = "skip_segments", columnDefinition = "TEXT")
    private String skipSegments; // 跳过片段记录

    @Column(name = "replay_segments", columnDefinition = "TEXT")
    private String replaySegments; // 重播片段记录

    @Column(name = "pause_points", columnDefinition = "TEXT")
    private String pausePoints; // 暂停点记录

    // 学习质量指标
    @Column(name = "attention_score")
    private Double attentionScore = 0.0; // 注意力评分 0-100

    @Column(name = "engagement_score")
    private Double engagementScore = 0.0; // 参与度评分 0-100

    @Column(name = "learning_efficiency")
    private Double learningEfficiency = 0.0; // 学习效率评分 0-100

    // 设备和环境信息
    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "browser_info", length = 200)
    private String browserInfo;

    @Column(name = "screen_resolution", length = 50)
    private String screenResolution;

    // 时间记录
    @Column(name = "first_watch_at")
    private LocalDateTime firstWatchAt;

    @Column(name = "last_watch_at")
    private LocalDateTime lastWatchAt;

    @Column(name = "total_sessions")
    private Integer totalSessions = 0; // 观看会话数

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 关系
    @OneToMany(mappedBy = "videoRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<VideoWatchSession> watchSessions;

    // 构造函数
    public VideoLearningRecord() {}

    public VideoLearningRecord(Long studentId, Long taskId, String videoUrl, Double videoDuration) {
        this.studentId = studentId;
        this.taskId = taskId;
        this.videoUrl = videoUrl;
        this.videoDuration = videoDuration;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Double getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(Double videoDuration) {
        this.videoDuration = videoDuration;
    }

    public Double getTotalWatchTime() {
        return totalWatchTime;
    }

    public void setTotalWatchTime(Double totalWatchTime) {
        this.totalWatchTime = totalWatchTime;
    }

    public Double getActualWatchTime() {
        return actualWatchTime;
    }

    public void setActualWatchTime(Double actualWatchTime) {
        this.actualWatchTime = actualWatchTime;
    }

    public Double getWatchProgress() {
        return watchProgress;
    }

    public void setWatchProgress(Double watchProgress) {
        this.watchProgress = watchProgress;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public String getWatchSegments() {
        return watchSegments;
    }

    public void setWatchSegments(String watchSegments) {
        this.watchSegments = watchSegments;
    }

    public String getSkipSegments() {
        return skipSegments;
    }

    public void setSkipSegments(String skipSegments) {
        this.skipSegments = skipSegments;
    }

    public String getReplaySegments() {
        return replaySegments;
    }

    public void setReplaySegments(String replaySegments) {
        this.replaySegments = replaySegments;
    }

    public String getPausePoints() {
        return pausePoints;
    }

    public void setPausePoints(String pausePoints) {
        this.pausePoints = pausePoints;
    }

    public Double getAttentionScore() {
        return attentionScore;
    }

    public void setAttentionScore(Double attentionScore) {
        this.attentionScore = attentionScore;
    }

    public Double getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(Double engagementScore) {
        this.engagementScore = engagementScore;
    }

    public Double getLearningEfficiency() {
        return learningEfficiency;
    }

    public void setLearningEfficiency(Double learningEfficiency) {
        this.learningEfficiency = learningEfficiency;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowserInfo() {
        return browserInfo;
    }

    public void setBrowserInfo(String browserInfo) {
        this.browserInfo = browserInfo;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }

    public LocalDateTime getFirstWatchAt() {
        return firstWatchAt;
    }

    public void setFirstWatchAt(LocalDateTime firstWatchAt) {
        this.firstWatchAt = firstWatchAt;
    }

    public LocalDateTime getLastWatchAt() {
        return lastWatchAt;
    }

    public void setLastWatchAt(LocalDateTime lastWatchAt) {
        this.lastWatchAt = lastWatchAt;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
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

    public List<VideoWatchSession> getWatchSessions() {
        return watchSessions;
    }

    public void setWatchSessions(List<VideoWatchSession> watchSessions) {
        this.watchSessions = watchSessions;
    }

    /**
     * 更新观看进度
     */
    public void updateProgress() {
        if (videoDuration != null && videoDuration > 0) {
            this.watchProgress = (actualWatchTime / videoDuration) * 100;
            this.completionRate = Math.min(watchProgress, 100.0);
        }
        this.lastWatchAt = LocalDateTime.now();
        if (this.firstWatchAt == null) {
            this.firstWatchAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "VideoLearningRecord{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", taskId=" + taskId +
                ", watchProgress=" + watchProgress +
                ", completionRate=" + completionRate +
                '}';
    }
} 