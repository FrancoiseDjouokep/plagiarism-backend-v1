package com.example.plagiarism1.controller;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.dto.*;
import com.example.plagiarism1.exception.AuthenticationException;
import com.example.plagiarism1.exception.ValidationException;
import com.example.plagiarism1.model.*;
import com.example.plagiarism1.repository.UtilisateurRepository;
import com.example.plagiarism1.service.JwtService;
import com.example.plagiarism1.service.PendingUserService;
import com.example.plagiarism1.service.UtilisateurService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
  Contrôleur principal gérant toutes les opérations liées aux utilisateurs et à l'authentification
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurController.class);

    private final UtilisateurService utilisateurService;
    private final PendingUserService pendingUserService; // Ajouté
    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    public UtilisateurController(
            UtilisateurService utilisateurService,
            PendingUserService pendingUserService,
            AuthenticationManager authenticationManager,
            UtilisateurRepository utilisateurRepository,
            JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.pendingUserService = pendingUserService; // Ajouté
        this.authenticationManager = authenticationManager;
        this.utilisateurRepository = utilisateurRepository;
        this.jwtService = jwtService;
    }


    @PostMapping("/inscription")
    public ResponseEntity<ApiResponse> inscription(@Valid @RequestBody InscriptionRequest request) {
        try {
            logger.info("Tentative d'inscription pour l'email: {}", request.getEmail());

            // Conversion du DTO en entité PendingUser
            PendingUser pendingUser = new PendingUser();
            pendingUser.setNom(request.getNom());
            pendingUser.setPrenom(request.getPrenom());
            pendingUser.setEmail(request.getEmail());
            pendingUser.setPassword(request.getPassword());

            // Gestion du rôle
            if (request.getRole() != null) {
                Role role = new Role();
                role.setLibelle(request.getRole());
                pendingUser.setRole(role);
            }

            // Appel au service
            pendingUserService.registerPendingUser(pendingUser);

            // Construction de la réponse selon le type d'utilisateur
            String message;
            if (request.getRole() == TypeDeRole.ETUDIANT || request.getRole() == null) {
                message = "Votre compte étudiant a été créé. Veuillez vérifier votre email pour activer votre compte.";
            } else {
                message = "Votre demande d'inscription en tant qu'enseignant a été enregistrée. Elle nécessite une validation administrative.";
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, message));

        } catch (RuntimeException e) {
            logger.error("Erreur lors de l'inscription: {}", e.getMessage());

            // Gestion des erreurs spécifiques
            String errorMessage;
            if (e.getMessage().contains("Email invalide")) {
                errorMessage = "L'adresse email est invalide.";
                return ResponseEntity.badRequest().body(new ApiResponse(false, errorMessage));
            } else if (e.getMessage().contains("déjà utilisé")) {
                errorMessage = "Cette adresse email est déjà utilisée.";
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse(false, errorMessage));
            } else if (e.getMessage().contains("Rôle spécifié non valide")) {
                errorMessage = "Le rôle spécifié n'est pas valide.";
                return ResponseEntity.badRequest().body(new ApiResponse(false, errorMessage));
            } else {
                errorMessage = "Une erreur est survenue lors de l'inscription.";
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, errorMessage));
            }
        }
    }
    /**
     * Activation d'un compte utilisateur
     */
    @PostMapping("/activation")
    public ResponseEntity<ApiResponse> activation(@RequestBody ActivationRequest request) {
        try {
            logger.info("Tentative d'activation avec le code: {}", request.getCode());

            // Création d'une Map pour garder la compatibilité avec le service existant
            Map<String, String> activationData = new HashMap<>();
            activationData.put("code", request.getCode());

            // Appel au service pour l'activation
            utilisateurService.activation(activationData);

            logger.info("Activation réussie avec le code: {}", request.getCode());
            return ResponseEntity.ok(new ApiResponse(true, "Votre compte a été activé avec succès."));
        } catch (ValidationException e) {
            logger.warn("Erreur de validation lors de l'activation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de l'activation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de l'activation du compte."));
        }
    }

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/connexion")
    public ResponseEntity<ApiResponse> connexion(@Valid @RequestBody AuthentificationDTO authentificationDTO) {
        try {
            logger.info("Tentative de connexion pour l'utilisateur: {}", authentificationDTO.username());

            // Vérification préalable si le compte existe et son statut d'activation
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(authentificationDTO.username());
            if (utilisateurOpt.isPresent() && !utilisateurOpt.get().isActif()) {
                logger.warn("Tentative de connexion à un compte non activé: {}", authentificationDTO.username());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "Compte non activé. Veuillez vérifier votre email pour activer votre compte."));
            }

            // Authentification via Spring Security
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authentificationDTO.username(),
                            authentificationDTO.password()
                    )
            );

            if (auth.isAuthenticated()) {
                // Récupérer l'utilisateur et son rôle
                Utilisateur utilisateur = utilisateurOpt.orElseThrow(() ->
                        new RuntimeException("Utilisateur non trouvé après authentification")
                );
                String role = utilisateur.getRole().getLibelle().name(); // e.g., "ADMIN", "USER", etc.

                // Génération des tokens JWT
                Map<String, String> tokens = jwtService.generate(authentificationDTO.username());

                // Ajouter le rôle à la réponse
                tokens.put("role", role); // Include role in the response

                logger.info("Connexion réussie pour l'utilisateur: {}", authentificationDTO.username());
                return ResponseEntity.ok(new ApiResponse(true, "Connexion réussie", tokens));
            } else {
                logger.warn("Échec d'authentification pour l'utilisateur: {}", authentificationDTO.username());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Échec de l'authentification"));
            }
        } catch (DisabledException e) {
            logger.warn("Tentative de connexion à un compte non activé: {}", authentificationDTO.username());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Compte non activé"));
        } catch (org.springframework.security.core.AuthenticationException e) {
            logger.warn("Identifiants invalides pour l'utilisateur: {}", authentificationDTO.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Identifiants invalides"));
        } catch (Exception e) {
            logger.error("Erreur lors de la connexion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de la connexion."));
        }
    }
    /**
     * Rafraîchissement du token JWT
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        try {
            logger.info("Tentative de rafraîchissement du token");

            // Appel au service pour rafraîchir le token
            Map<String, String> tokens = jwtService.refreshToken(refreshTokenRequest);

            logger.info("Token rafraîchi avec succès");
            return ResponseEntity.ok(new ApiResponse(true, "Token rafraîchi avec succès", tokens));
        } catch (Exception e) {
            logger.warn("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Déconnexion d'un utilisateur
     */
    @PostMapping("/deconnexion")
    public ResponseEntity<ApiResponse> deconnexion() {
        logger.info("Déconnexion d'un utilisateur");

        // Appel au service pour la déconnexion
        jwtService.deconnexion();

        return ResponseEntity.ok(new ApiResponse(true, "Déconnexion réussie"));
    }

    /**
     * Demande de réinitialisation de mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> demandeResetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            logger.info("Demande de réinitialisation de mot de passe pour l'email: {}", request.getEmail());

            // Appel au service pour initier la réinitialisation
            utilisateurService.demandeResetPassword(request.getEmail());

            // Pour des raisons de sécurité, toujours retourner le même message
            return ResponseEntity.ok(new ApiResponse(true,
                    "Si votre email existe dans notre système, vous recevrez un code de réinitialisation."));
        } catch (Exception e) {
            logger.error("Erreur lors de la demande de réinitialisation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de la demande de réinitialisation."));
        }
    }

    /**
     * Réinitialisation du mot de passe
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            logger.info("Tentative de réinitialisation de mot de passe avec le code: {}", request.getCode());

            // Création d'une Map pour garder la compatibilité avec le service existant
            Map<String, String> resetData = new HashMap<>();
            resetData.put("code", request.getCode());
            resetData.put("password", request.getPassword());

            // Appel au service pour finaliser la réinitialisation
            utilisateurService.resetPassword(resetData);

            logger.info("Réinitialisation de mot de passe réussie");
            return ResponseEntity.ok(new ApiResponse(true, "Votre mot de passe a été réinitialisé avec succès."));
        } catch (ValidationException e) {
            logger.warn("Erreur de validation lors de la réinitialisation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de la réinitialisation du mot de passe: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Une erreur est survenue lors de la réinitialisation du mot de passe."));
        }
    }
    @GetMapping("/me")
    public ResponseEntity<UtilisateurDTO> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        // 1. Extraire le token
        String token = authHeader.substring(7); // Supprimer "Bearer "

        // 2. Valider le token via votre JwtService
        Jwt jwt = jwtService.tokenByValue(token);

        // 3. Récupérer l'utilisateur
        Utilisateur utilisateur = jwt.getUtilisateur();

        // 4. Convertir en DTO
        return ResponseEntity.ok(mapToDTO(utilisateur));
    }

    private UtilisateurDTO mapToDTO(Utilisateur utilisateur) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setEmail(utilisateur.getEmail());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        // Exclure les champs sensibles comme password
        return dto;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/select")
    public List<Utilisateur> list() {
        return utilisateurService.list();
    }

}