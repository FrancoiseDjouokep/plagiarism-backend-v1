package com.example.plagiarism1.controller;

import com.example.plagiarism1.dto.ApiResponse;
import com.example.plagiarism1.dto.PendingUserDTO;
import com.example.plagiarism1.dto.RejetRequest;
import com.example.plagiarism1.model.PendingUser;
import com.example.plagiarism1.repository.PendingUserRepository;
import com.example.plagiarism1.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminValidationController {

    private static final Logger logger = LoggerFactory.getLogger(AdminValidationController.class);

    private final AdminService adminService;
    private final PendingUserRepository pendingUserRepository;

    public AdminValidationController(AdminService adminService,
                                     PendingUserRepository pendingUserRepository) {
        this.adminService = adminService;
        this.pendingUserRepository = pendingUserRepository;
    }

    /**
     * Liste toutes les inscriptions en attente
     */
    @GetMapping("/pending-users")
    public ResponseEntity<List<PendingUserDTO>> listerInscriptionsEnAttente() {
        List<PendingUser> users = pendingUserRepository.findAllByEmailVerifiedTrue();
        List<PendingUserDTO> dtos = users.stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    private PendingUserDTO convertToDTO(PendingUser user) {
        return new PendingUserDTO(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getLibelle().name() : null
        );
    }

    /**
     * Valide une inscription
     */
    @PostMapping("/valider-inscription/{pendingUserId}")
    public ResponseEntity<ApiResponse> validerInscription(@PathVariable Long pendingUserId) {
        try {
            adminService.validerInscription(pendingUserId);
            logger.info("Inscription validée pour l'ID: {}", pendingUserId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Inscription validée avec succès"));
        } catch (Exception e) {
            logger.error("Erreur lors de la validation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Rejette une inscription
     */
    @PostMapping("/rejeter-inscription/{pendingUserId}")
    public ResponseEntity<ApiResponse> rejeterInscription(
            @PathVariable Long pendingUserId,
            @RequestBody(required = false) RejetRequest request) {

        try {
            String raison = (request != null && request.getRaison() != null) ?
                    request.getRaison() : "Raison non spécifiée";

            adminService.rejeterInscription(pendingUserId, raison);
            logger.info("Inscription rejetée pour l'ID: {}", pendingUserId);
            return ResponseEntity.ok(
                    new ApiResponse(true, "Inscription rejetée avec succès"));
        } catch (Exception e) {
            logger.error("Erreur lors du rejet: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}