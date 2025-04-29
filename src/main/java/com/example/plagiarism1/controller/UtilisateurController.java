package com.example.plagiarism1.controller;

import ch.qos.logback.classic.Logger;
import com.example.plagiarism1.AuthentificationDTO;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.service.JwtService;
import com.example.plagiarism1.service.UtilisateurService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public class UtilisateurController {
    private UtilisateurService utilisateurService;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    public UtilisateurController(UtilisateurService utilisateurService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping(path = "inscription")
    public ResponseEntity<Map<String, String>> inscription(@RequestBody Utilisateur utilisateur) {
        try {
            System.out.println("Inscription appelée !");
            this.utilisateurService.inscription(utilisateur);
            return ResponseEntity.ok(Map.of("message", "Inscription réussie"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping(path = "activation")
    public void activation(@RequestBody Map<String, String> activation){

        this.utilisateurService.activation(activation);
    }

    @PostMapping(path = "refresh-token")
    public @ResponseBody Map<String, String> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        return this.jwtService.refreshToken(refreshTokenRequest);
    }

    @PostMapping(path = "deconnexion")
    public void deconnexion(){

        this.jwtService.deconnexion();
    }
    @PostMapping(path = "connexion")
    public ResponseEntity<Map<String, String>> connexion(@RequestBody AuthentificationDTO authentificationDTO) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authentificationDTO.username(),
                            authentificationDTO.password()
                    )
            );

            if (auth.isAuthenticated()) {
                return ResponseEntity.ok(jwtService.generate(authentificationDTO.username()));
            }
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Compte non activé"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Identifiants invalides"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Endpoint to request a password reset
     * @param requestBody Map containing user email
     * @return Response with status
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> demandeResetPassword(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            // For security, always return the same message whether the email exists or not
            utilisateurService.demandeResetPassword(email);
            return ResponseEntity.ok("Si votre email existe dans notre système, vous recevrez un code de réinitialisation");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur est survenue lors de la demande de réinitialisation");
        }
    }

    /**
     * Endpoint to reset password with validation code
     * @param resetPasswordData Map containing code and new password
     * @return Response with status
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> resetPasswordData) {
        try {
            utilisateurService.resetPassword(resetPasswordData);
            return ResponseEntity.ok("Votre mot de passe a été réinitialisé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur est survenue lors de la réinitialisation du mot de passe");
        }
    }
}
