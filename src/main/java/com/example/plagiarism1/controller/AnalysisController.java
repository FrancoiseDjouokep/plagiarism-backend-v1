package com.example.plagiarism1.controller;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.service.AnalysisService;
import com.example.plagiarism1.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }
    @PostMapping
    public ResponseEntity<Analysis> analise(
            @RequestParam("file") @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("id") long targetDocumentId) {
        Analysis analisedDocument = analysisService.JaccardDistance(file, title, targetDocumentId);
        return ResponseEntity.ok(analisedDocument);
    }
}
