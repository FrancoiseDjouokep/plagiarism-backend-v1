package com.example.plagiarism1.controller;


import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.dto.*;
import com.example.plagiarism1.model.Role;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /**
     * Register a new user
     * @param request Registration data
     * @return Response with status
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> inscription(@Valid @RequestBody InscriptionRequest request) {
        try {
            // Convert DTO to entity
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(request.getNom());
            utilisateur.setEmail(request.getEmail());
            utilisateur.setPassword(request.getPassword());

            // Set default role
            Role defaultRole = new Role();
            defaultRole.setLibelle(TypeDeRole.ETUDIANT);
            utilisateur.setRole(defaultRole);

            // Call service for registration
//            utilisateurService.inscription(utilisateur);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Inscription réussie. Veuillez vérifier votre email pour activer votre compte."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de l'inscription."));
        }
    }

    /**
     * Activate an account using a validation code
     * @param request Activation request with code
     * @return Response with status
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse> activation(@RequestBody ActivationRequest request) {
        try {
            Map<String, String> activationData = new HashMap<>();
            activationData.put("code", request.getCode());

            utilisateurService.activation(activationData);

            return ResponseEntity.ok(new ApiResponse(true, "Votre compte a été activé avec succès."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de l'activation du compte."));
        }
    }

    /**
     * Request password reset
     * @param request Password reset request with email
     * @return Response with status
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> demandeResetPassword(@RequestBody PasswordResetRequest request) {
        try {
            utilisateurService.demandeResetPassword(request.getEmail());

            // For security purposes, always return the same message
            return ResponseEntity.ok(new ApiResponse(true,
                    "Si votre email existe dans notre système, vous recevrez un code de réinitialisation."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de la demande de réinitialisation."));
        }
    }

    /**
     * Reset password with validation code
     * @param request Reset password request with code and new password
     * @return Response with status
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            Map<String, String> resetData = new HashMap<>();
            resetData.put("code", request.getCode());
            resetData.put("password", request.getPassword());

            utilisateurService.resetPassword(resetData);

            return ResponseEntity.ok(new ApiResponse(true, "Votre mot de passe a été réinitialisé avec succès."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de la réinitialisation du mot de passe."));
        }
    }
}