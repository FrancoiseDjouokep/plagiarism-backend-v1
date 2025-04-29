package com.example.plagiarism1.dto;


/**
 * DTO for account activation requests
 */
public class ActivationRequest {
    private String code;

    // Constructors
    public ActivationRequest() {
    }

    public ActivationRequest(String code) {
        this.code = code;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ActivationRequest{" +
                "code='" + code + '\'' +
                '}';
    }
}
