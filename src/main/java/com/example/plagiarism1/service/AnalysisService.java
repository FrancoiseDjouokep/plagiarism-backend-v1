package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.AnalysisRepository;
import com.example.plagiarism1.repository.DocumentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {
    private final DocumentRepository repository;
    private final DocumentService documentService;
    private final AnalysisRepository analysisRepository;

    public AnalysisService(DocumentRepository repository, DocumentService documentService, AnalysisRepository analysisRepository) {
        this.repository = repository;
        this.documentService = documentService;
        this.analysisRepository = analysisRepository;
    }

    public Analysis JaccardDistance(MultipartFile file, String title, long targetDocumentId) {
        Document documentExitant = repository.findById(targetDocumentId).orElse(null);
        if (documentExitant == null) {
            throw new RuntimeException("Le document n'existe pas dans la base de données.");
        }

        Document uploadedDoc = documentService.uploadDocument(file, title);

        // Récupération des n-grams stockés
        String[] ngrams1 = documentExitant.getNgrams().split("\\s*\\|\\s*");
        String[] ngrams2 = uploadedDoc.getNgrams().split("\\s*\\|\\s*");

        Map<String, Integer> ngram1 = new HashMap<>();
        Map<String, Integer> ngram2 = new HashMap<>();

        for (String ng : ngrams1) {
            ngram1.put(ng, ngram1.getOrDefault(ng, 0) + 1);
        }

        for (String ng : ngrams2) {
            ngram2.put(ng, ngram2.getOrDefault(ng, 0) + 1);
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
        ana.setSimilarityScore(similarity);
        ana.setTargetDocumentId(documentExitant.getId());
        Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ana.setUtilisateur(utilisateur);

        return analysisRepository.save(ana);
    }

    public List<Analysis> getAllAnalysis() {
        return analysisRepository.findAll();
    }

}
