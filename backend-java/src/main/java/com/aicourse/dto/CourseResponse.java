package com.aicourse.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程响应DTO
 */
public class CourseResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String semester;
    private Integer year;
    private Double credits;
    private String status;
    private Integer maxStudents;
    private Boolean isActive;
    private String category;
    private String difficulty;
    private Double price;
    private List<String> tags;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverImage;
    private String syllabusUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 额外统计信息
    private Integer studentCount;
    private Integer taskCount;
    private Double averageScore;
    private Integer progress;
    private Boolean enrolled;

    public CourseResponse() {}

    // Builder pattern
    public static CourseResponse builder() {
        return new CourseResponse();
    }

    public CourseResponse id(Long id) {
        this.id = id;
        return this;
    }

    public CourseResponse name(String name) {
        this.name = name;
        return this;
    }

    public CourseResponse code(String code) {
        this.code = code;
        return this;
    }

    public CourseResponse description(String description) {
        this.description = description;
        return this;
    }

    public CourseResponse teacherId(Long teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public CourseResponse teacherName(String teacherName) {
        this.teacherName = teacherName;
        return this;
    }

    public CourseResponse semester(String semester) {
        this.semester = semester;
        return this;
    }

    public CourseResponse year(Integer year) {
        this.year = year;
        return this;
    }

    public CourseResponse credits(Double credits) {
        this.credits = credits;
        return this;
    }

    public CourseResponse status(String status) {
        this.status = status;
        return this;
    }

    public CourseResponse maxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
        return this;
    }

    public CourseResponse isActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public CourseResponse category(String category) {
        this.category = category;
        return this;
    }

    public CourseResponse difficulty(String difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public CourseResponse price(Double price) {
        this.price = price;
        return this;
    }

    public CourseResponse tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public CourseResponse startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public CourseResponse endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public CourseResponse coverImage(String coverImage) {
        this.coverImage = coverImage;
        return this;
    }

    public CourseResponse syllabusUrl(String syllabusUrl) {
        this.syllabusUrl = syllabusUrl;
        return this;
    }

    public CourseResponse createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public CourseResponse updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public CourseResponse studentCount(Integer studentCount) {
        this.studentCount = studentCount;
        return this;
    }

    public CourseResponse taskCount(Integer taskCount) {
        this.taskCount = taskCount;
        return this;
    }

    public CourseResponse averageScore(Double averageScore) {
        this.averageScore = averageScore;
        return this;
    }

    public CourseResponse progress(Integer progress) {
        this.progress = progress;
        return this;
    }

    public CourseResponse enrolled(Boolean enrolled) {
        this.enrolled = enrolled;
        return this;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getCredits() {
        return credits;
    }

    public void setCredits(Double credits) {
        this.credits = credits;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getSyllabusUrl() {
        return syllabusUrl;
    }

    public void setSyllabusUrl(String syllabusUrl) {
        this.syllabusUrl = syllabusUrl;
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

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Boolean getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Boolean enrolled) {
        this.enrolled = enrolled;
    }

    @Override
    public String toString() {
        return "CourseResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", semester='" + semester + '\'' +
                ", year=" + year +
                ", credits=" + credits +
                ", status='" + status + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", studentCount=" + studentCount +
                ", taskCount=" + taskCount +
                '}';
    }
} 