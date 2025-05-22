package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.AnalysisRepository;
import com.example.plagiarism1.repository.DocumentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class AnalysisService {
    private final DocumentRepository repository;
    private final DocumentService documentService;
    private final AnalysisRepository analysisRepository;
    private final BigAnalysisService bigAnalysisService;

    public AnalysisService(DocumentRepository repository, DocumentService documentService, AnalysisRepository analysisRepository, BigAnalysisService bigAnalysisService) {
        this.repository = repository;
        this.documentService = documentService;
        this.analysisRepository = analysisRepository;
        this.bigAnalysisService = bigAnalysisService;
    }
    public List<Analysis> getAllAnalysis() {
        return analysisRepository.findAll();
    }

    public Analysis JaccardDistance(MultipartFile file, String title, String targetDocumentTitle) {
        // Recherche du document par titre
        List<Document> existingDocs = repository.searchByTitle(targetDocumentTitle);
        if (existingDocs.isEmpty()) {
            throw new RuntimeException("Aucun document trouvé avec ce titre.");
        }

        Document targetDoc = existingDocs.get(0);
        Document uploadedDoc = documentService.uploadDocument(file, title);

        // Logging pour débogage
        System.out.println("Uploaded doc ngrams: " + uploadedDoc.getNgrams());
        System.out.println("Target doc ngrams: " + targetDoc.getNgrams());

        // Génération des n-grammes en map avec normalisation
        String[] ngramsUploaded = uploadedDoc.getNgrams().split("\\|");
        Map<String, Integer> ngram2 = new HashMap<>();
        for (String ng : ngramsUploaded) {
            String normalizedNg = ng.trim().toLowerCase();
            if (!normalizedNg.isEmpty()) {
                ngram2.put(normalizedNg, ngram2.getOrDefault(normalizedNg, 0) + 1);
            }
        }

        String[] ngramsTarget = targetDoc.getNgrams().split("\\|");
        Map<String, Integer> ngram1 = new HashMap<>();
        for (String ng : ngramsTarget) {
            String normalizedNg = ng.trim().toLowerCase();
            if (!normalizedNg.isEmpty()) {
                ngram1.put(normalizedNg, ngram1.getOrDefault(normalizedNg, 0) + 1);
            }
        }

        // Logging pour débogage
        System.out.println("Ngram1 keys: " + ngram1.keySet());
        System.out.println("Ngram2 keys: " + ngram2.keySet());

        // Intersection des n-grammes
        Set<String> suspectNgrams = new HashSet<>();
        for (String ngram : ngram1.keySet()) {
            if (ngram2.containsKey(ngram)) {
                suspectNgrams.add(ngram);
            }
        }

        // Logging pour débogage
        System.out.println("Intersection size: " + suspectNgrams.size());
        System.out.println("Ngram1 size: " + ngram1.size());
        System.out.println("Ngram2 size: " + ngram2.size());

        double similarity = ((double) suspectNgrams.size() / Math.max(ngram1.size(), ngram2.size())) * 100;

        // Logging pour débogage
        System.out.println("Calculated similarity: " + similarity);

        // Extraire les phrases contenant des n-grammes suspects
        List<String> phrasesSource = bigAnalysisService.extractMatchingSentences(uploadedDoc.getContent(), suspectNgrams);
        List<String> phrasesTarget = bigAnalysisService.extractMatchingSentences(targetDoc.getContent(), suspectNgrams);

        // Créer et sauvegarder l'analyse
        Analysis ana = new Analysis();
        ana.setCreationDate(LocalDateTime.now());
        ana.setSourceDocumentId(uploadedDoc.getId());
        ana.setTargetDocumentId(targetDoc.getId());
        ana.setSimilarityScore(similarity);
        ana.setUtilisateur((Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        ana.setSuspectPhrasesSource(phrasesSource);
        ana.setSuspectPhrasesTarget(phrasesTarget);

        return analysisRepository.save(ana);
    }

}
