package com.example.plagiarism1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'authentification d'un utilisateur
 * Cette classe imite le comportement d'un record Java
 */
@Schema(description = "Informations d'authentification utilisateur")
public class AuthentificationDTO {

    @Schema(description = "Email de l'utilisateur (identifiant)", example = "utilisateur@example.com", required = true)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private final String username;

    @Schema(description = "Mot de passe de l'utilisateur", example = "motdepasse123", required = true, minLength = 6)
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private final String password;

    /**
     * Constructeur par défaut requis pour la désérialisation JSON
     */
    public AuthentificationDTO() {
        this.username = null;
        this.password = null;
    }

    /**
     * Constructeur pour AuthentificationDTO
     *
     * @param username l'email de l'utilisateur (utilisé comme identifiant)
     * @param password le mot de passe
     */
    public AuthentificationDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Retourne l'identifiant utilisateur (email)
     *
     * @return l'email
     */
    public String username() {
        return this.username;
    }

    /**
     * Retourne le mot de passe
     *
     * @return le mot de passe
     */
    public String password() {
        return this.password;
    }

    /**
     * Getter standard pour compatibilité avec les frameworks
     *
     * @return l'email (username)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter standard pour compatibilité avec les frameworks
     *
     * @return le mot de passe
     */
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "AuthentificationDTO{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}