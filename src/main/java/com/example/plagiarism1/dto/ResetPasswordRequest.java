package com.example.plagiarism1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for resetting password with code (second step)
 */
public class ResetPasswordRequest {
    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    // Constructors
    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String code, String password) {
        this.code = code;
        this.password = password;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "code='" + code + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}