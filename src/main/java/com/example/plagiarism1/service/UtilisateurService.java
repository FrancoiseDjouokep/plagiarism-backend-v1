package com.example.plagiarism1.service;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.model.Role;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import com.example.plagiarism1.repository.JwtRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class UtilisateurService implements UserDetailsService {
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
            defaultRole.setLibelle("UTILISATEUR");
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

        if (code == null || nouveauPassword == null || nouveauPassword.length() < 6) {
            throw new RuntimeException("Données de réinitialisation invalides");
        }

        // Verify the validation code
        Validation validation = validationService.lireEnFonctionDuCode(code);

        // Check if the code is expired
        if (Instant.now().isAfter(validation.getExpire())) {
            throw new RuntimeException("Votre code de réinitialisation a expiré");
        }

        // Get the user and update password
        Utilisateur utilisateur = validation.getUtilisateur();

        // Remove associated JWTs before updating
        jwtRepository.deleteByUtilisateurId(utilisateur.getId());

        utilisateur.setPassword(passwordEncoder.encode(nouveauPassword));
        utilisateurRepository.save(utilisateur);

        // Delete the used validation code
        validationService.supprimer(validation);
    }
}
