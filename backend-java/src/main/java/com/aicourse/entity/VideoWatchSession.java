package com.aicourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 视频观看会话记录实体类
 */
@Entity
@Table(name = "video_watch_sessions")
public class VideoWatchSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "video_record_id", nullable = false)
    private Long videoRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_record_id", insertable = false, updatable = false)
    @JsonIgnore
    private VideoLearningRecord videoRecord;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId; // 会话唯一标识

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "watch_duration")
    private Double watchDuration = 0.0; // 本次会话观看时长

    // 会话内的行为数据（存储为JSON字符串）
    @Column(name = "play_events", columnDefinition = "TEXT")
    private String playEvents; // 播放事件

    @Column(name = "pause_events", columnDefinition = "TEXT")
    private String pauseEvents; // 暂停事件

    @Column(name = "seek_events", columnDefinition = "TEXT")
    private String seekEvents; // 跳转事件

    @Column(name = "speed_events", columnDefinition = "TEXT")
    private String speedEvents; // 倍速事件

    // 会话质量指标
    @Column(name = "focus_intervals", columnDefinition = "TEXT")
    private String focusIntervals; // 专注时间段

    @Column(name = "distraction_count")
    private Integer distractionCount = 0; // 分心次数

    @Column(name = "average_speed")
    private Double averageSpeed = 1.0; // 平均播放速度

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 构造函数
    public VideoWatchSession() {}

    public VideoWatchSession(Long videoRecordId, String sessionId) {
        this.videoRecordId = videoRecordId;
        this.sessionId = sessionId;
        this.startTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVideoRecordId() {
        return videoRecordId;
    }

    public void setVideoRecordId(Long videoRecordId) {
        this.videoRecordId = videoRecordId;
    }

    public VideoLearningRecord getVideoRecord() {
        return videoRecord;
    }

    public void setVideoRecord(VideoLearningRecord videoRecord) {
        this.videoRecord = videoRecord;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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

    public Double getWatchDuration() {
        return watchDuration;
    }

    public void setWatchDuration(Double watchDuration) {
        this.watchDuration = watchDuration;
    }

    public String getPlayEvents() {
        return playEvents;
    }

    public void setPlayEvents(String playEvents) {
        this.playEvents = playEvents;
    }

    public String getPauseEvents() {
        return pauseEvents;
    }

    public void setPauseEvents(String pauseEvents) {
        this.pauseEvents = pauseEvents;
    }

    public String getSeekEvents() {
        return seekEvents;
    }

    public void setSeekEvents(String seekEvents) {
        this.seekEvents = seekEvents;
    }

    public String getSpeedEvents() {
        return speedEvents;
    }

    public void setSpeedEvents(String speedEvents) {
        this.speedEvents = speedEvents;
    }

    public String getFocusIntervals() {
        return focusIntervals;
    }

    public void setFocusIntervals(String focusIntervals) {
        this.focusIntervals = focusIntervals;
    }

    public Integer getDistractionCount() {
        return distractionCount;
    }

    public void setDistractionCount(Integer distractionCount) {
        this.distractionCount = distractionCount;
    }

    public Double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 结束会话
     */
    public void endSession() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            // 计算会话总时长（分钟）
            long duration = java.time.Duration.between(this.startTime, this.endTime).toMinutes();
            this.watchDuration = (double) duration;
        }
    }

    @Override
    public String toString() {
        return "VideoWatchSession{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", watchDuration=" + watchDuration +
                ", distractionCount=" + distractionCount +
                '}';
    }
} 