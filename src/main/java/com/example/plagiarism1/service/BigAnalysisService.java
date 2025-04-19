package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.repository.AnalysisRepository;
import com.example.plagiarism1.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BigAnalysisService {
    private final DocumentRepository repository;
    private final DocumentService documentService;
    private final AnalysisRepository analysisRepository;

    public BigAnalysisService(DocumentRepository repository, DocumentService documentService, AnalysisRepository analysisRepository) {
        this.repository = repository;
        this.documentService = documentService;
        this.analysisRepository = analysisRepository;
    }


    public List<Analysis> compareWithAllDocuments(MultipartFile file, String title) {
        Document uploadedDoc = documentService.uploadDocument(file, title);
        String language2 = uploadedDoc.getLanguage();

        Map<String, Integer> ngram2 = new HashMap<>();
        int n = 3;
        String[] token2 = uploadedDoc.getContent().split(" ");
        for (int i = 0; i <= token2.length - n; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = 0; j < n; j++) {
                ngram.append(token2[i + j]);
            }
            String ngramStr = ngram.toString().trim();
            ngram2.put(ngramStr, ngram2.getOrDefault(ngramStr, 0) + 1);
        }

        List<Analysis> analyses = new ArrayList<>();
        List<Document> documentsExistants = repository.findAll();

        for (Document doc : documentsExistants) {
            String[] token1;
            if (language2.equals(doc.getLanguage())) {
                token1 = doc.getContent().split(" ");
            } else {
                token1 = doc.getTranslatedContent().split(" ");
            }

            Map<String, Integer> ngram1 = new HashMap<>();
            for (int i = 0; i <= token1.length - n; i++) {
                StringBuilder ngram = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    ngram.append(token1[i + j]);
                }
                String ngramStr = ngram.toString().trim();
                ngram1.put(ngramStr, ngram1.getOrDefault(ngramStr, 0) + 1);
            }

            int intersection = 0;
            for (String ngram : ngram1.keySet()) {
                if (ngram2.containsKey(ngram)) {
                    intersection++;
                }
            }

            double similarity = ((double) intersection / Math.max(ngram1.size(), ngram2.size())) * 100;

            Analysis ana = new Analysis();
            ana.setCreationDate(LocalDateTime.now());
            ana.setSourceDocumentId(uploadedDoc.getId());
            ana.setTargetDocumentId(doc.getId());
            ana.setSimilarityScore(similarity);
            double score = ana.getSimilarityScore();
            if (score >= 20){
                analyses.add(analysisRepository.save(ana));
            }

        }

        return analyses;
    }
}
