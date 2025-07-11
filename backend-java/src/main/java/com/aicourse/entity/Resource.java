package com.aicourse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String url;
    private Long courseId;
    private Long uploaderId;
    private LocalDateTime uploadTime;
    private String description;
    private Boolean visibleToAll = true; // true: 全体学生可见，false: 仅本班

    // getter/setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getVisibleToAll() { return visibleToAll; }
    public void setVisibleToAll(Boolean visibleToAll) { this.visibleToAll = visibleToAll; }
} 