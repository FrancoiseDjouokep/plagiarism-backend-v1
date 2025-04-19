package com.example.plagiarism1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;

    private long sourceDocumentId;

    private long targetDocumentId;

    private double similarityScore;
    private  int nGramSize;
    private double threshold;
    private LocalDateTime creationDate;
    private Boolean isCrossLanguage;
    private  String status;

    public Analysis(long id, String status, Boolean isCrossLanguage, LocalDateTime creationDate, double threshold, int nGramSize, double similarityScore, long targetDocumentId, long sourceDocumentId) {
        this.id = id;
        this.status = status;
        this.isCrossLanguage = isCrossLanguage;
        this.creationDate = creationDate;
        this.threshold = threshold;
        this.nGramSize = nGramSize;
        this.similarityScore = similarityScore;
        this.targetDocumentId = targetDocumentId;
        this.sourceDocumentId = sourceDocumentId;
    }

    public Analysis() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getCrossLanguage() {
        return isCrossLanguage;
    }

    public void setCrossLanguage(Boolean crossLanguage) {
        isCrossLanguage = crossLanguage;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getnGramSize() {
        return nGramSize;
    }

    public void setnGramSize(int nGramSize) {
        this.nGramSize = nGramSize;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public long getTargetDocumentId() {
        return targetDocumentId;
    }

    public void setTargetDocumentId(long targetDocumentId) {
        this.targetDocumentId = targetDocumentId;
    }

    public long getSourceDocumentId() {
        return sourceDocumentId;
    }

    public void setSourceDocumentId(long sourceDocumentId) {
        this.sourceDocumentId = sourceDocumentId;
    }
}
