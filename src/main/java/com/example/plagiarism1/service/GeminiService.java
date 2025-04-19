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
        String prompt = "\"Analyse ce texte et décide s’il a été généré par une IA ou écrit par un humain. Réponds uniquement par : IA ou Humain.  \n" + "Si tu n’es pas sûr, choisis 'Humain'.\"\n\n\nTexte : \"" + texte + "\"";

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("parts", Collections.singletonList(part));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", Collections.singletonList(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                    return parts.get(0).get("text").trim(); // "IA" ou "Humain"
                } else {
                    return "Aucune réponse générée.";
                }
            } else {
                return "Erreur dans la réponse de l'API.";
            }

        } catch (Exception e) {
            return "Erreur lors de l’analyse : " + e.getMessage();
        }
    }
}
