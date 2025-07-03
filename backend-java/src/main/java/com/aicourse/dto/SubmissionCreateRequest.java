





package com.aicourse.dto;

import org.springframework.web.multipart.MultipartFile;

public class SubmissionCreateRequest {
    private Long taskId;
    private Long studentId;
    private String content;
    private MultipartFile file;
    private Integer attemptNumber;

    // getter/setter
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
} 