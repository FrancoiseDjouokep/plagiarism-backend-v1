package com.example.plagiarism1.controller;

import com.example.plagiarism1.dto.DetailedAnalysisResponse;
import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.repository.AnalysisRepository;
import com.example.plagiarism1.service.AnalysisService;
import com.example.plagiarism1.service.BigAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bigAnalysis")
public class BigAnalysisController {
    private final BigAnalysisService bigAnalysisService;
    private final AnalysisRepository analysisRepository;

    public BigAnalysisController(BigAnalysisService bigAnalysisService, AnalysisRepository analysisRepository) {
        this.bigAnalysisService = bigAnalysisService;
        this.analysisRepository = analysisRepository;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "analyse a document")
    public ResponseEntity<List<Analysis>> analise(
            @RequestParam("file") @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary") MultipartFile file,
            @RequestParam("title") String title) {
        List<Analysis> analisedDocument = bigAnalysisService.compareWithAllDocuments(file, title);
        return ResponseEntity.ok(analisedDocument);
    }
    @GetMapping("/analyses/{id}/suspect-phrases")
    public ResponseEntity<DetailedAnalysisResponse> getSuspectPhrases(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean highlight) {

            DetailedAnalysisResponse response = bigAnalysisService.getDetailedAnalysis(id, highlight);
            return ResponseEntity.ok(response);
    }
}
