package com.aicourse.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "classes")
public class Classroom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "code", length = 50, unique = true)
    private String code;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "grade_level", length = 50)
    private String gradeLevel;
    
    @Column(name = "major", length = 100)
    private String major;
    
    @Column(name = "advisor_id")
    private Long advisorId;
    
    @Column(name = "student_count")
    private Integer studentCount = 0;
    
    @Column(name = "max_students")
    private Integer maxStudents = 50;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关系映射
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id", insertable = false, updatable = false)
    private User advisor;
    
    @OneToMany(mappedBy = "classId", fetch = FetchType.LAZY)
    private List<User> students;
    
    // 构造函数
    public Classroom() {}
    
    public Classroom(String name, String code, String description, String gradeLevel, 
                    String major, Long advisorId, Integer maxStudents) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.gradeLevel = gradeLevel;
        this.major = major;
        this.advisorId = advisorId;
        this.maxStudents = maxStudents;
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
    
    public String getGradeLevel() {
        return gradeLevel;
    }
    
    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public Long getAdvisorId() {
        return advisorId;
    }
    
    public void setAdvisorId(Long advisorId) {
        this.advisorId = advisorId;
    }
    
    public Integer getStudentCount() {
        return studentCount;
    }
    
    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
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
    
    public User getAdvisor() {
        return advisor;
    }
    
    public void setAdvisor(User advisor) {
        this.advisor = advisor;
    }
    
    public List<User> getStudents() {
        return students;
    }
    
    public void setStudents(List<User> students) {
        this.students = students;
    }
    
    @Override
    public String toString() {
        return "Classroom{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", major='" + major + '\'' +
                ", advisorId=" + advisorId +
                ", studentCount=" + studentCount +
                ", maxStudents=" + maxStudents +
                ", isActive=" + isActive +
                '}';
    }
} 