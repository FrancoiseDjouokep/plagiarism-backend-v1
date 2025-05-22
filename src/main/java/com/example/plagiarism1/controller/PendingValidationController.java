package com.example.plagiarism1.controller;

import com.example.plagiarism1.model.PendingUser;
import com.example.plagiarism1.model.PendingValidation;
import com.example.plagiarism1.repository.PendingUserRepository;
import com.example.plagiarism1.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PendingValidationController {

    private final ValidationService validationService;
    private final PendingUserRepository pendingUserRepository;

    public PendingValidationController(ValidationService validationService,
                                       PendingUserRepository pendingUserRepository) {
        this.validationService = validationService;
        this.pendingUserRepository = pendingUserRepository;
    }

    @PostMapping("/verifier-email")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        String code = request.get("code");

        // Valider le code
        PendingValidation validation = validationService.lireEnFonctionDuCodePending(code);

        // Marquer l'email comme vérifié
        PendingUser pendingUser = validation.getPendingUser();
        pendingUser.setEmailVerified(true);
        pendingUserRepository.save(pendingUser);

        return ResponseEntity.ok("Email vérifié avec succès. Votre compte est en attente d'approbation par l'administrateur.");
    }
}