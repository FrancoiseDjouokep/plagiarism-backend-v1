package com.example.plagiarism1.model;

import jakarta.persistence.*;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
@Entity
@Table(name = "Documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;

    private String title;

    private String filename;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    private String language ;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String translatedContent;
    private String translationLanguage;
    private LocalDateTime uploadDate;
    private long fileSize;
    private  int wordCount;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String ngrams;
    @ManyToOne
    private Utilisateur utilisateur;

    public Document(long id, int wordCount, long fileSize, LocalDateTime uploadDate, String translationLanguage, String language, String content, String translatedContent, String filename, String title, String ngrams, Utilisateur utilisateur) {
        this.id = id;
        this.wordCount = wordCount;
        this.fileSize = fileSize;
        this.uploadDate = uploadDate;
        this.translationLanguage = translationLanguage;
        this.language = language;
        this.content = content;
        this.translatedContent = translatedContent;
        this.filename = filename;
        this.title = title;
        this.ngrams = ngrams;
        this.utilisateur = utilisateur;
    }

    public Document() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getTranslationLanguage() {
        return translationLanguage;
    }

    public void setTranslationLanguage(String translationLanguage) {
        this.translationLanguage = translationLanguage;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNgrams() {
        return ngrams;
    }

    public void setNgrams(String ngrams) {
        this.ngrams = ngrams;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
