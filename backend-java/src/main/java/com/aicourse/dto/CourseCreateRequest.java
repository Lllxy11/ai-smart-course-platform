package com.aicourse.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 课程创建请求DTO
 */
public class CourseCreateRequest {

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 255, message = "课程名称长度不能超过255个字符")
    private String name;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @Size(max = 50, message = "课程代码长度不能超过50个字符")
    private String code;

    @Size(max = 2000, message = "课程描述长度不能超过2000个字符")
    private String description;

    @Size(max = 100, message = "学期长度不能超过100个字符")
    private String semester;

    @Min(value = 2020, message = "年份不能小于2020")
    @Max(value = 2030, message = "年份不能大于2030")
    private Integer year;

    @DecimalMin(value = "0.5", message = "学分不能小于0.5")
    @DecimalMax(value = "10.0", message = "学分不能大于10.0")
    private Double credits;

    @Pattern(regexp = "DRAFT|PUBLISHED|ARCHIVED", message = "状态必须是DRAFT, PUBLISHED或ARCHIVED")
    private String status = "DRAFT";

    @Min(value = 1, message = "最大学生数不能小于1")
    @Max(value = 1000, message = "最大学生数不能大于1000")
    private Integer maxStudents = 50;

    @Size(max = 100, message = "分类长度不能超过100个字符")
    private String category;

    @Pattern(regexp = "beginner|intermediate|advanced", message = "难度必须是beginner, intermediate或advanced")
    private String difficulty = "intermediate";

    @DecimalMin(value = "0.0", message = "价格不能小于0")
    private Double price = 0.0;

    private List<String> tags;

    private LocalDate startDate;
    private LocalDate endDate;

    @Pattern(regexp = "^$|^https?://.*", message = "封面图片必须是有效的URL或为空")
    private String coverImage;

    @Pattern(regexp = "^$|^https?://.*", message = "教学大纲必须是有效的URL或为空")
    private String syllabusUrl;

    public CourseCreateRequest() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
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

    @Override
    public String toString() {
        return "CourseCreateRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", semester='" + semester + '\'' +
                ", year=" + year +
                ", credits=" + credits +
                ", status='" + status + '\'' +
                ", maxStudents=" + maxStudents +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", price=" + price +
                '}';
    }
} 