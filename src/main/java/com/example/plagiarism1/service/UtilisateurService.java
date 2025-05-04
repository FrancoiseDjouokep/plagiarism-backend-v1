package com.example.plagiarism1.service;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.dto.UtilisateurDTO;
import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Role;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import com.example.plagiarism1.repository.JwtRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UtilisateurService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private UtilisateurRepository utilisateurRepository;
    private JwtRepository jwtRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, JwtRepository jwtRepository, BCryptPasswordEncoder passwordEncoder, ValidationService validationService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtRepository=jwtRepository;
        this.validationService = validationService;
    }

    public void inscription(Utilisateur utilisateur) {
        // Validate email
        if (!utilisateur.getEmail().matches("[^@]+@[^@]+\\.[^@]+")) {
            throw new RuntimeException("Email invalide");
        }

        // Check if email exists
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // Hash password
        utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));

        // Set default role if not provided
        if (utilisateur.getRole() == null) {
            Role defaultRole = new Role();
            defaultRole.setLibelle(TypeDeRole.ETUDIANT);
            utilisateur.setRole(defaultRole);
        }

        utilisateurRepository.save(utilisateur);
        validationService.enregistrer(utilisateur);
    }

    public void activation(Map<String, String> activation) {
        Validation validation = this.validationService.lireEnFonctionDuCode(activation.get("code"));
        if(Instant.now().isAfter(validation.getExpire())){
            throw new RuntimeException("Votre delai est expirer");
        }
        Utilisateur utilisateurActiver= this.utilisateurRepository.findById(validation.getUtilisateur().getId()).orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));
        utilisateurActiver.setActif(true);
        this.utilisateurRepository.save(utilisateurActiver);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.utilisateurRepository
                .findByEmail(username)
                .filter(Utilisateur::isActif) // Add this check
                .orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond a cette identifiant ou le compte n'est pas activé"));
    }

    /**
     * Start the password reset process by sending an email with a validation code
     * @param email The user's email address
     */
    public void demandeResetPassword(String email) {
        Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findByEmail(email);

        if (utilisateurOptional.isEmpty()) {
            // For security reasons, don't reveal that the email doesn't exist
            return;
        }

        Utilisateur utilisateur = utilisateurOptional.get();
        // Generate and send validation code
        validationService.enregistrer(utilisateur);
    }

    /**
     * Complete the password reset process with the validation code and new password
     * @param resetPasswordData Map containing the code and new password
     */

    @Transactional
    public void resetPassword(Map<String, String> resetPasswordData) {
        String code = resetPasswordData.get("code");
        String nouveauPassword = resetPasswordData.get("password");

        // Validation
        if (code == null || nouveauPassword == null || nouveauPassword.length() < 8) {
            throw new RuntimeException("Données invalides");
        }

        // 1. Trouver la validation sans supprimer quoi que ce soit
        Validation validation = validationService.lireEnFonctionDuCode(code);
        if (validation == null) {
            throw new RuntimeException("Code invalide");
        }

        // 2. Vérifier l'expiration
        if (Instant.now().isAfter(validation.getExpire())) {
            throw new RuntimeException("Code expiré");
        }

        // 3. Récupérer l'utilisateur
        Utilisateur utilisateur = validation.getUtilisateur();
        if (utilisateur == null) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        // 4. Journalisation avant modification
        logger.info("Reset password pour utilisateur ID: {}", utilisateur.getId());

        // 5. Supprimer les JWT existants
        jwtRepository.deleteByUtilisateurId(utilisateur.getId());

        // 6. Mettre à jour le mot de passe
        utilisateur.setPassword(passwordEncoder.encode(nouveauPassword));
        utilisateurRepository.save(utilisateur);

        // 7. NE PAS supprimer l'utilisateur - seulement la validation
        validationService.supprimer(validation); // Doit seulement supprimer l'entrée Validation

        logger.info("Mot de passe mis à jour pour utilisateur ID: {}", utilisateur.getId());
    }

    public List<Utilisateur> list() {
        final Iterable<Utilisateur> utilisateurIterable = this.utilisateurRepository.findAll();
        List utilisateurs = new ArrayList();
        for (Utilisateur utilisateur : utilisateurIterable) {
              utilisateurs.add(utilisateur);
        }
        return utilisateurs;
    }
}
