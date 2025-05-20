package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
@Service
public class GeminiService {

    private static final String API_KEY = "AIzaSyDZChVxdfx7hZyouAKzqcG31YUI5E2LWVQ";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + API_KEY;

    public String detectAi(String texte) {
        RestTemplate restTemplate = new RestTemplate();

        // Prompt optimisé pour une détection précise
        String prompt = """
            Analyse ce texte et détermine s'il a été généré par une IA (comme GPT, Gemini, etc.) ou écrit par un humain.
            Sois attentif aux signes suivants :
            - Répétitions excessives
            - Structure trop parfaite
            - Manque de profondeur émotionnelle
            - Phrases génériques
            - Patterns prévisibles

            Réponds UNIQUEMENT par l'un des mots suivants :
            - "IA" si le texte est clairement généré par une IA.
            - "Humain" si le texte est authentiquement humain.
            - "Incertain" si tu ne peux pas trancher.

            Texte à analyser :
            """ + texte;

        // Construction de la requête
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("parts", Collections.singletonList(part));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", Collections.singletonList(message));
        body.put("generationConfig", Map.of(
                "temperature", 0.0,  // Réduit la créativité pour plus de rigueur
                "topP", 0.1,
                "maxOutputTokens", 10
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL, request, Map.class);
            return extractResponse(response.getBody());
        } catch (Exception e) {
            return "Erreur : " + e.getMessage();
        }
    }

    private String extractResponse(Map<String, Object> responseBody) {
        if (responseBody == null) return "Erreur : réponse vide";

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
        if (candidates == null || candidates.isEmpty()) return "Aucune réponse générée";

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
        String response = parts.get(0).get("text").trim();

        // Validation stricte de la réponse
        return response.matches("IA|Humain|Incertain") ? response : "Incertain";
    }
}