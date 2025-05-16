package com.example.plagiarism1.service;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.DocumentRepository;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DocumentService {

    private final DocumentRepository repository;
    private final Tika tika;
    private static final String API_URL = "https://ws.detectlanguage.com/0.2/detect";
    private static final String API_KEY = "fd2135f5bddc86c89cd1ad8d9a469c54";
    private final RestTemplate restTemplate;

    public DocumentService(DocumentRepository repository, Tika tika, RestTemplate restTemplate, DocumentRepository documentRepository) {
        this.repository = repository;
        this.tika = tika;
        this.restTemplate = restTemplate;

    }

    public Document uploadDocument(MultipartFile file, String title) {

        try {
            String fullText = tika.parseToString(file.getInputStream()).replaceAll("\\r?\\n", "");

            String extractedText = filterContent(fullText);
            String language = detectLanguage(extractedText);
            String source = "en";
             String target = "fr";
            Document doc = new Document();
            doc.setTitle(title);
            doc.setFilename(file.getOriginalFilename());
            doc.setContent(extractedText);
            doc.setUploadDate(LocalDateTime.now());
            doc.setFileSize(file.getSize());
            doc.setLanguage(language);
            if ("fr".equals(language)){
                doc.setTranslatedContent(null);
                doc.setTranslationLanguage(null);
            }
            else {
                doc.setTranslatedContent(translate(extractedText,source,target));
                doc.setTranslationLanguage(target);
            }
            doc.setWordCount(new String(extractedText).split(" ").length);
            int n = 5;
            String[] tokens;

            if ("fr".equals(language)) {
                tokens = extractedText.split("\\s+");
                doc.setNgrams(generateNgrams(tokens, n));
            } else {
                String translated = doc.getTranslatedContent();
                if (translated != null) {
                    tokens = translated.split("\\s+");
                    doc.setNgrams(generateNgrams(tokens, n));
                } else {
                    doc.setNgrams(null);
                }
            }

            Utilisateur utilisateur = (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            doc.setUtilisateur(utilisateur);
            return repository.save(doc);
        } catch (IOException | TikaException e) {
            throw new RuntimeException("Erreur lors du traitement du fichier", e);
        }
    }

    public String detectLanguage(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(API_KEY);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("q", text);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> detections = (List<Map<String, Object>>) data.get("detections");
            if (!detections.isEmpty()) {
                return (String) detections.get(0).get("language");
            }
        }
        throw new RuntimeException("Impossible de détecter la langue.");
    }


    public String translate(String text, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                    sourceLang, targetLang, encodedText
            );

            String response = restTemplate.getForObject(url, String.class);
            System.out.println("Réponse brute de traduction : " + response);

            if (response != null && response.startsWith("[[[\"")) {

                return response.split("\"")[1];
            } else {
                return "Réponse inattendue de l'API Google Translate.";
            }
        } catch (Exception e) {
            System.out.println("Erreur traduction : " + e.getMessage());
            e.printStackTrace();
            return "Erreur traduction : " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }


    private String filterContent(String fullText) {

        String normalizedText = fullText.replaceAll("\\r?\\n", " ").replaceAll("\\s+", " ").trim();

        String[] introductionMarkers = {
                "introduction",
                "1 introduction",
                "chapter 1",
                "1. introduction",
                "1.0 introduction"
        };

        for (String marker : introductionMarkers) {
            int introIndex = normalizedText.toLowerCase().indexOf(marker.toLowerCase());
            if (introIndex > 0) {
                return normalizedText.substring(introIndex);
            }
        }

        String[] words = normalizedText.split("\\s+");
        if (words.length > 500) {
            return String.join(" ", Arrays.copyOfRange(words, 500, words.length));
        }

        return normalizedText;
    }


    public List<Document> getAllDocuments() {
        return repository.findAll();
    }
    public void deleteDocument(Long id)  {
        repository.deleteById(id);
    }
    private String generateNgrams(String[] tokens, int n) {
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= tokens.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(tokens[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }
        return String.join(" | ", ngrams); // séparateur visible
    }


}
