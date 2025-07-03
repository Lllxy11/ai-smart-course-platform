package com.aicourse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Token响应DTO
 */
public class TokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType = "bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;

    public TokenResponse() {}

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public TokenResponse(String accessToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
} 