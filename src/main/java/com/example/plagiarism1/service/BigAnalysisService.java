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
import java.util.stream.Collectors;
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
                List<String> phrasesSource = extractMatchingSentences(uploadedDoc.getContent(), suspectNgrams);
                List<String> phrasesTarget = extractMatchingSentences(doc.getContent(), suspectNgrams);

                Analysis ana = new Analysis();
                ana.setCreationDate(LocalDateTime.now());
                ana.setSourceDocumentId(uploadedDoc.getId());
                ana.setTargetDocumentId(doc.getId());
                ana.setSourceDocumentTitle(uploadedDoc.getTitle());
                ana.setTargetDocumentTitle(doc.getTitle());
                ana.setSimilarityScore(similarity);
                ana.setUtilisateur((Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                ana.setSuspectPhrasesSource(phrasesSource);
                ana.setSuspectPhrasesTarget(phrasesTarget);

                analyses.add(analysisRepository.save(ana));
            }
        }

        return analyses;
    }

    public List<String> extractMatchingSentences(String content, Set<String> suspectNgrams) {
        List<String> matchingSentences = new ArrayList<>();

        // Split content into sentences (simple regex - adjust as needed)
        String[] sentences = content.split("(?<=[.!?])\\s+");

        for (String sentence : sentences) {
            // Check if sentence contains ANY suspect n-gram
            for (String ngram : suspectNgrams) {
                if (sentence.contains(ngram)) {
                    matchingSentences.add(sentence);
                    break; // No need to check other ngrams for this sentence
                }
            }
        }
        return matchingSentences;
    }

    public DetailedAnalysisResponse getDetailedAnalysis(Long analysisId, boolean highlight) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));

        Document source = repository.findById(analysis.getSourceDocumentId())
                .orElseThrow(() -> new RuntimeException("Source document not found"));

        Document target = repository.findById(analysis.getTargetDocumentId())
                .orElseThrow(() -> new RuntimeException("Target document not found"));

        DetailedAnalysisResponse response = new DetailedAnalysisResponse();
        response.setSourceFullText(source.getContent());

        // Highlight matching sentences in source text
        response.setSourcePhrases(Collections.singletonList(highlightMatches(
                source.getContent(),
                analysis.getSuspectPhrasesSource(),
                true
        )));

        DetailedAnalysisResponse.SimilarDocumentDTO dto = new DetailedAnalysisResponse.SimilarDocumentDTO();
        dto.setId(target.getId());
        dto.setTitle(target.getTitle());

        // Highlight matching sentences in target text
        dto.setMatchedPhrases(Collections.singletonList(highlightMatches(
                target.getContent(),
                analysis.getSuspectPhrasesTarget(),
                false
        )));

        response.setSimilarDocuments(List.of(dto));
        return response;
    }


    private Map<String, Integer> phraseToIdMap = new HashMap<>();
    private int currentMatchId = 1;

    private String highlightMatches(String fullText, List<String> matchingSentences, boolean isSourceDocument) {
        String highlighted = fullText;

        if (isSourceDocument) {
            phraseToIdMap.clear();
            currentMatchId = 1;
        }

        for (String sentence : matchingSentences) {
            int matchId;
            if (phraseToIdMap.containsKey(sentence)) {
                // Utiliser l'ID existant pour cette phrase
                matchId = phraseToIdMap.get(sentence);
            } else {
                // Créer un nouvel ID pour cette phrase
                matchId = currentMatchId++;
                phraseToIdMap.put(sentence, matchId);
            }

            highlighted = highlighted.replace(
                    sentence,
                            "[" + matchId + "] " + sentence +
                            " "
            );
        }
        return highlighted;
    }

}
