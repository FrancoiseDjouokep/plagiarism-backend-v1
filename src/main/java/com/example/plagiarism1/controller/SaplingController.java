package com.example.plagiarism1.controller;

import com.example.plagiarism1.service.SaplingDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detect-ai")
public class SaplingController {

    @Autowired
    private SaplingDetectionService detectionService;

    @PostMapping
    public String detectText(@RequestBody String text) {
        double prob = detectionService.detectAIProbability(text);
        return "Probabilité que ce texte soit généré par une IA : " + (prob * 100) + " %";
    }
}
