package com.example.plagiarism1.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @ElementCollection
    @CollectionTable(name = "analysis_suspect_phrases_source", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(length = 20000)
    private List<String> suspectPhrasesSource;

    @ElementCollection
    @CollectionTable(name = "analysis_suspect_phrases_target", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(length = 20000)
    private List<String> suspectPhrasesTarget;

    @ManyToOne
    private Utilisateur utilisateur;

    public Analysis(long id, String status, Boolean isCrossLanguage, LocalDateTime creationDate, double threshold, int nGramSize, double similarityScore, long targetDocumentId, long sourceDocumentId, List<String> suspectPhrasesSource, List<String> suspectPhrasesTarget, Utilisateur utilisateur) {
        this.id = id;
        this.status = status;
        this.isCrossLanguage = isCrossLanguage;
        this.creationDate = creationDate;
        this.threshold = threshold;
        this.nGramSize = nGramSize;
        this.similarityScore = similarityScore;
        this.targetDocumentId = targetDocumentId;
        this.sourceDocumentId = sourceDocumentId;
        this.suspectPhrasesSource = suspectPhrasesSource;
        this.suspectPhrasesTarget = suspectPhrasesTarget;
        this.utilisateur = utilisateur;
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

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public List<String> getSuspectPhrasesSource() {
        return suspectPhrasesSource;
    }

    public void setSuspectPhrasesSource(List<String> suspectPhrasesSource) {
        this.suspectPhrasesSource = suspectPhrasesSource;
    }

    public List<String> getSuspectPhrasesTarget() {
        return suspectPhrasesTarget;
    }

    public void setSuspectPhrasesTarget(List<String> suspectPhrasesTarget) {
        this.suspectPhrasesTarget = suspectPhrasesTarget;
    }

}
