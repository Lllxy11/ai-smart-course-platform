package com.aicourse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 试卷题目关联实体类
 */
@Entity
@Table(name = "exam_questions")
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "exam_paper_id", nullable = false)
    private Long examPaperId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_paper_id", insertable = false, updatable = false)
    @JsonIgnore
    private ExamPaper examPaper;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    @JsonIgnore
    private Question question;

    // 题目配置
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex; // 题目顺序

    @Column(name = "points", nullable = false)
    private Double points; // 题目分值

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 构造函数
    public ExamQuestion() {}

    public ExamQuestion(Long examPaperId, Long questionId, Integer orderIndex, Double points) {
        this.examPaperId = examPaperId;
        this.questionId = questionId;
        this.orderIndex = orderIndex;
        this.points = points;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(Long examPaperId) {
        this.examPaperId = examPaperId;
    }

    public ExamPaper getExamPaper() {
        return examPaper;
    }

    public void setExamPaper(ExamPaper examPaper) {
        this.examPaper = examPaper;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ExamQuestion{" +
                "id=" + id +
                ", examPaperId=" + examPaperId +
                ", questionId=" + questionId +
                ", orderIndex=" + orderIndex +
                ", points=" + points +
                '}';
    }
} 