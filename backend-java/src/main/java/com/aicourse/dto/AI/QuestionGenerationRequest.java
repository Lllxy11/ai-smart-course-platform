package com.aicourse.dto.AI;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * AI题目生成请求DTO
 */
public class QuestionGenerationRequest {

    @NotBlank(message = "主题不能为空")
    private String topic;

    @Pattern(regexp = "easy|medium|hard", message = "难度必须是easy, medium或hard")
    private String difficulty = "medium";

    @Min(value = 1, message = "题目数量不能小于1")
    @Max(value = 20, message = "题目数量不能大于20")
    private Integer count = 5;

    @Pattern(regexp = "multiple_choice|true_false|short_answer|essay", 
             message = "题目类型必须是multiple_choice, true_false, short_answer或essay")
    private String questionType = "multiple_choice";

    public QuestionGenerationRequest() {}

    public QuestionGenerationRequest(String topic, String difficulty, Integer count, String questionType) {
        this.topic = topic;
        this.difficulty = difficulty;
        this.count = count;
        this.questionType = questionType;
    }

    // Getters and Setters
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    @Override
    public String toString() {
        return "QuestionGenerationRequest{" +
                "topic='" + topic + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", count=" + count +
                ", questionType='" + questionType + '\'' +
                '}';
    }
} 