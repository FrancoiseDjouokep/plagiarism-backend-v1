package com.example.plagiarism1.dto;

import java.util.List;

public class DetailedAnalysisResponse {
    private String sourceFullText;
    private List<String> sourcePhrases;
    private List<SimilarDocumentDTO> similarDocuments;

    public DetailedAnalysisResponse() {

    }

    public DetailedAnalysisResponse(String sourceFullText, List<String> sourcePhrases, List<SimilarDocumentDTO> similarDocuments) {
        this.sourceFullText = sourceFullText;
        this.sourcePhrases = sourcePhrases;
        this.similarDocuments = similarDocuments;
    }

    public String getSourceFullText() {
        return sourceFullText;
    }

    public void setSourceFullText(String sourceFullText) {
        this.sourceFullText = sourceFullText;
    }

    public List<String> getSourcePhrases() {
        return sourcePhrases;
    }

    public void setSourcePhrases(List<String> sourcePhrases) {
        this.sourcePhrases = sourcePhrases;
    }

    public List<SimilarDocumentDTO> getSimilarDocuments() {
        return similarDocuments;
    }

    public void setSimilarDocuments(List<SimilarDocumentDTO> similarDocuments) {
        this.similarDocuments = similarDocuments;
    }

    public static class SimilarDocumentDTO {
        private Long id;
        private String title;
        private List<String> matchedPhrases;

        public SimilarDocumentDTO() {

        }

        public SimilarDocumentDTO(Long id, String title, List<String> matchedPhrases) {
            this.id = id;
            this.title = title;
            this.matchedPhrases = matchedPhrases;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public List<String> getMatchedPhrases() {
            return matchedPhrases;
        }

        public void setMatchedPhrases(List<String> matchedPhrases) {
            this.matchedPhrases = matchedPhrases;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}

