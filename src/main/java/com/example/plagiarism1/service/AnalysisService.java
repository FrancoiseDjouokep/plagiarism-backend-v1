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

@Transactional
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

    public Analysis JaccardDistance(MultipartFile file, String title, String targetDocumentTitle) {
        // Recherche du document par titre
        List<Document> existingDocs = repository.searchByTitle(targetDocumentTitle);
        if (existingDocs.isEmpty()) {
            throw new RuntimeException("Aucun document trouvé avec ce titre.");
        }

        Document targetDoc = existingDocs.get(0);
        Document uploadedDoc = documentService.uploadDocument(file, title);

        // Génération des n-grammes en map
        String[] ngramsUploaded = uploadedDoc.getNgrams().split("\\s*\\|\\s*");
        Map<String, Integer> ngram2 = new HashMap<>();
        for (String ng : ngramsUploaded) {
            ngram2.put(ng, ngram2.getOrDefault(ng, 0) + 1);
        }

        String[] ngramsTarget = targetDoc.getNgrams().split("\\s*\\|\\s*");
        Map<String, Integer> ngram1 = new HashMap<>();
        for (String ng : ngramsTarget) {
            ngram1.put(ng, ngram1.getOrDefault(ng, 0) + 1);
        }

        // Intersection des n-grammes
        Set<String> suspectNgrams = new HashSet<>();
        for (String ngram : ngram1.keySet()) {
            if (ngram2.containsKey(ngram)) {
                suspectNgrams.add(ngram);
            }
        }

        double similarity = ((double) suspectNgrams.size() / Math.max(ngram1.size(), ngram2.size())) * 100;


        // Extraire les phrases contenant des n-grammes suspects
        List<String> phrasesSource = bigAnalysisService.extractMatchingSentences(uploadedDoc.getContent(), suspectNgrams);
        List<String> phrasesTarget = bigAnalysisService.extractMatchingSentences(targetDoc.getContent(), suspectNgrams);

        // Créer et sauvegarder l’analyse
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

    public List<Analysis> getAllAnalysis() {
        return analysisRepository.findAll();
    }

}
