package com.example.plagiarism1.service;

import com.example.plagiarism1.dto.DetailedAnalysisResponse;
import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.AnalysisRepository;
import com.example.plagiarism1.repository.DocumentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;@Service
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

        String[] ngramsUploaded = uploadedDoc.getNgrams().split("\\s*\\|\\s*");
        Map<String, Integer> ngram2 = new HashMap<>();
        for (String ng : ngramsUploaded) {
            ngram2.put(ng, ngram2.getOrDefault(ng, 0) + 1);
        }

        List<Analysis> analyses = new ArrayList<>();
        List<Document> documentsExistants = repository.findAll()
                .stream()
                .filter(doc -> doc.getId() != uploadedDoc.getId())
                .collect(Collectors.toList());

        for (Document doc : documentsExistants) {
            String[] ngramsDoc = doc.getNgrams().split("\\s*\\|\\s*");
            Map<String, Integer> ngram1 = new HashMap<>();
            for (String ng : ngramsDoc) {
                ngram1.put(ng, ngram1.getOrDefault(ng, 0) + 1);
            }

            Set<String> suspectNgrams = new HashSet<>();
            for (String ngram : ngram1.keySet()) {
                if (ngram2.containsKey(ngram)) {
                    suspectNgrams.add(ngram);
                }
            }

            double similarity = ((double) suspectNgrams.size() / Math.max(ngram1.size(), ngram2.size())) * 100;

            if (similarity >= 30) {
                List<String> phrasesSource = extractMatchingPhrases(uploadedDoc.getContent(), suspectNgrams);
                List<String> phrasesTarget = extractMatchingPhrases(doc.getContent(), suspectNgrams);

                Analysis ana = new Analysis();
                ana.setCreationDate(LocalDateTime.now());
                ana.setSourceDocumentId(uploadedDoc.getId());
                ana.setTargetDocumentId(doc.getId());
                ana.setSimilarityScore(similarity);
                ana.setUtilisateur((Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                ana.setSuspectPhrasesSource(phrasesSource);
                ana.setSuspectPhrasesTarget(phrasesTarget);

                analyses.add(analysisRepository.save(ana));
            }
        }

        return analyses;
    }

    private List<String> extractMatchingPhrases(String content, Set<String> ngrams) {
        List<String> suspects = new ArrayList<>();
        String[] tokens = content.split("\\s+");
        int n = 5;
        for (int i = 0; i <= tokens.length - n; i++) {
            String phrase = String.join(" ", Arrays.copyOfRange(tokens, i, i + n)).trim();
            if (ngrams.contains(phrase)) {
                suspects.add(phrase);
            }
        }
        return suspects;
    }

    public DetailedAnalysisResponse getDetailedAnalysis(Long analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));

        Document source = repository.findById(analysis.getSourceDocumentId())
                .orElseThrow(() -> new RuntimeException("Source document not found"));

        Document target = repository.findById(analysis.getTargetDocumentId())
                .orElseThrow(() -> new RuntimeException("Target document not found"));

        DetailedAnalysisResponse response = new DetailedAnalysisResponse();
        response.setSourceFullText(source.getContent());
        response.setSourcePhrases(analysis.getSuspectPhrasesSource());

        DetailedAnalysisResponse.SimilarDocumentDTO dto = new DetailedAnalysisResponse.SimilarDocumentDTO();
        dto.setId(target.getId());
        dto.setTitle(target.getTitle());
        dto.setMatchedPhrases(analysis.getSuspectPhrasesTarget());

        response.setSimilarDocuments(List.of(dto));
        return response;
    }

    // 🔥 Nouveau service pour retourner toutes les similarités avec le document source
    public DetailedAnalysisResponse getAllDetailedAnalyses(Long uploadedDocId) {
        Document uploaded = repository.findById(uploadedDocId)
                .orElseThrow(() -> new RuntimeException("Uploaded document not found"));

        List<Analysis> allAnalyses = analysisRepository.findBySourceDocumentId(uploadedDocId);

        DetailedAnalysisResponse response = new DetailedAnalysisResponse();
        response.setSourceFullText(uploaded.getContent());

        Set<String> allSourcePhrases = allAnalyses.stream()
                .flatMap(ana -> ana.getSuspectPhrasesSource().stream())
                .collect(Collectors.toSet());
        response.setSourcePhrases(new ArrayList<>(allSourcePhrases));

        List<DetailedAnalysisResponse.SimilarDocumentDTO> documents = new ArrayList<>();
        for (Analysis ana : allAnalyses) {
            Document target = repository.findById(ana.getTargetDocumentId())
                    .orElseThrow(() -> new RuntimeException("Target document not found"));

            DetailedAnalysisResponse.SimilarDocumentDTO dto = new DetailedAnalysisResponse.SimilarDocumentDTO();
            dto.setId(target.getId());
            dto.setTitle(target.getTitle());
            dto.setMatchedPhrases(ana.getSuspectPhrasesTarget());

            documents.add(dto);
        }

        response.setSimilarDocuments(documents);
        return response;
    }
}
