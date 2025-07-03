package com.aicourse.dto;

public class SubmissionGradeRequest {
    private Double score;
    private String feedback;
    private Long graderId;

    // getter/setter
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Long getGraderId() { return graderId; }
    public void setGraderId(Long graderId) { this.graderId = graderId; }
} 