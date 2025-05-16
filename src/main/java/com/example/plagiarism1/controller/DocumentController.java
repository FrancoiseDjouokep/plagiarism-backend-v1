package com.example.plagiarism1.controller;

import com.example.plagiarism1.dto.DocumentTitleDTO;
import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.repository.DocumentRepository;
import com.example.plagiarism1.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRepository repository;
    public DocumentController(DocumentService documentService, DocumentRepository repository) {
        this.documentService = documentService;
        this.repository = repository;
    }

    @PostMapping("/upload")
    @io.swagger.v3.oas.annotations.Operation(summary = "Upload a document")
    public ResponseEntity<Document> upload(
            @RequestParam("file") @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary") MultipartFile file,
            @RequestParam("title") String title) {
        Document uploadedDocument = documentService.uploadDocument(file, title);
        return ResponseEntity.ok(uploadedDocument);
    }

    @GetMapping("/select")
    public List<Document> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/search")
    public ResponseEntity<List<DocumentTitleDTO>> searchDocuments(@RequestParam String query) {
        return ResponseEntity.ok(repository.searchByTitlePrefix(query));
    }

}
