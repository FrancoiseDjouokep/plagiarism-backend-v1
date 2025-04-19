package com.example.plagiarism1.controller;

import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    @io.swagger.v3.oas.annotations.Operation(summary = "Upload a document")
    public ResponseEntity<Document> upload(
            @RequestParam("file") @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary") MultipartFile file,
            @RequestParam("title") String title) {
        Document uploadedDocument = documentService.uploadDocument(file, title);
        return ResponseEntity.ok(uploadedDocument);
    }

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
