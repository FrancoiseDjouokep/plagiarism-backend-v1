package com.example.plagiarism1.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
public class SaplingDetectionService {

    private final String API_URL = "https://api.sapling.ai/api/v1/aidetect";
    private final String API_KEY = "FYTQIL30ZVZHCOQKBQ07H0HUJ5LSU9IC"; // ta clé privée

    public double detectAIProbability(String text) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", API_KEY);
        requestBody.put("text", text);

        // En-têtes
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Appel à l'API
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> body = response.getBody();
            System.out.println("RESPONSE BODY: " + body);
            return body.get("ai_probability") != null ? (Double) body.get("ai_probability") : 0.0;
        } else {
            throw new RuntimeException("Erreur lors de la détection : " + response.getStatusCode());
        }
    }
}


