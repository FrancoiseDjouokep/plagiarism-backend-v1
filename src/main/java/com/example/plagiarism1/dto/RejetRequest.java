package com.example.plagiarism1.dto;

import lombok.Data;

@Data

public class RejetRequest {
    private String raison;


    public RejetRequest() {
    }
    public RejetRequest(String raison) {
        this.raison = raison;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }
}