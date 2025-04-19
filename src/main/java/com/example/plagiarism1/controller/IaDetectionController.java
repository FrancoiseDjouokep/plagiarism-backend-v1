package com.example.plagiarism1.controller;

import com.example.plagiarism1.service.GeminiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IaDetectionController {

    private final GeminiService geminiService;

    public IaDetectionController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/detect")
    @io.swagger.v3.oas.annotations.Operation(summary = "analyse a document")
    public ResponseEntity<String> detect( @RequestBody String texte) {
        String result = geminiService.detectAi(texte);

        if (result.contains("Erreur") || result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'analyse du texte.");
        }
        return ResponseEntity.ok(result);
    }
}

