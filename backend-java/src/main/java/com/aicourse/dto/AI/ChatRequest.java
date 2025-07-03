package com.aicourse.dto.AI;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * AI对话请求DTO
 */
public class ChatRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000个字符")
    private String message;

    private List<Map<String, String>> context;

    private Boolean useReasoner = false;

    public ChatRequest() {}

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, List<Map<String, String>> context, Boolean useReasoner) {
        this.message = message;
        this.context = context;
        this.useReasoner = useReasoner;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Map<String, String>> getContext() {
        return context;
    }

    public void setContext(List<Map<String, String>> context) {
        this.context = context;
    }

    public Boolean getUseReasoner() {
        return useReasoner;
    }

    public void setUseReasoner(Boolean useReasoner) {
        this.useReasoner = useReasoner;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", useReasoner=" + useReasoner +
                ", contextSize=" + (context != null ? context.size() : 0) +
                '}';
    }
} 