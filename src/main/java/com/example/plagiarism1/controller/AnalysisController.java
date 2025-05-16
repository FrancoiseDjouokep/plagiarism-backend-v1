package com.example.plagiarism1.controller;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.service.AnalysisService;
import com.example.plagiarism1.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(value = "/compare-by-title", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Analysis> analyzeDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("targetTitle") String targetDocumentTitle) {

        Analysis analysis = analysisService.JaccardDistance(file, title, targetDocumentTitle);
        return ResponseEntity.ok(analysis);
    }
}