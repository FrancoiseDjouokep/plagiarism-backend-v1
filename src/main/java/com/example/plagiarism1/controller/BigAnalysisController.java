package com.example.plagiarism1.controller;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.service.AnalysisService;
import com.example.plagiarism1.service.BigAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bigAnalysis")
public class BigAnalysisController {
    private final BigAnalysisService bigAnalysisService;

    public BigAnalysisController(BigAnalysisService bigAnalysisService) {
        this.bigAnalysisService = bigAnalysisService;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "analyse a document")
    public ResponseEntity<List<Analysis>> analise(
            @RequestParam("file") @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary") MultipartFile file,
            @RequestParam("title") String title) {
        List<Analysis> analisedDocument = bigAnalysisService.compareWithAllDocuments(file, title);
        return ResponseEntity.ok(analisedDocument);
    }

}
