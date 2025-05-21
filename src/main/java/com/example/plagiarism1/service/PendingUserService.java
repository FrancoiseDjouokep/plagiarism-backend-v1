package com.example.plagiarism1.service;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.model.*;
import com.example.plagiarism1.repository.JwtRepository;
import com.example.plagiarism1.repository.PendingUserRepository;
import com.example.plagiarism1.repository.RoleRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendingUserService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private UtilisateurRepository utilisateurRepository;
    private PendingUserRepository pendingUserRepository; // Ajouté
    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;
    private final NotificationService notificationService;
    private  final RoleRepository roleRepository;

    public PendingUserService(UtilisateurRepository utilisateurRepository,
                              PendingUserRepository pendingUserRepository, // Ajouté
                              BCryptPasswordEncoder passwordEncoder,
                              ValidationService validationService, NotificationService notificationService, RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.pendingUserRepository = pendingUserRepository; // Ajouté
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.notificationService = notificationService;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void registerPendingUser(PendingUser pendingUser) {
        // 1. Vérification et récupération du rôle
        TypeDeRole roleType = pendingUser.getRole() != null
                ? pendingUser.getRole().getLibelle()
                : TypeDeRole.ETUDIANT;

        Role role = roleRepository.findByLibelle(roleType)
                .orElseThrow(() -> new RuntimeException("Rôle " + roleType + " introuvable"));

        pendingUser.setRole(role);

        // 2. Validation email
        if (pendingUser.getEmail() == null || !pendingUser.getEmail().matches("[^@]+@[^@]+\\.[^@]+")) {
            throw new RuntimeException("Email invalide");
        }

        // 3. Vérification email existant
        if (utilisateurRepository.findByEmail(pendingUser.getEmail()).isPresent() ||
                pendingUserRepository.findByEmail(pendingUser.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé ou en attente de validation");
        }

        // 4. Hashage du mot de passe
        pendingUser.setPassword(passwordEncoder.encode(pendingUser.getPassword()));

        // 5. Gestion selon le rôle
        if (roleType == TypeDeRole.ETUDIANT) {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(pendingUser.getNom());
            utilisateur.setPrenom(pendingUser.getPrenom());
            utilisateur.setEmail(pendingUser.getEmail());
            utilisateur.setPassword(pendingUser.getPassword());
            utilisateur.setRole(role); // Utilisez le rôle persisté
            utilisateur.setActif(false);

            utilisateurRepository.save(utilisateur); // Sauvegarder d'abord
            Validation validation = validationService.enregistrer(utilisateur); // Puis créer la validation
            notificationService.envoyer(validation);
        } else {
            PendingUser savedUser = pendingUserRepository.save(pendingUser);
            PendingValidation validation = validationService.enregistrer(savedUser);
            notificationService.envoyerPendingUserValidation(validation);
            notificationService.notifierAdminNouvelleInscription(savedUser);
        }
    }
}
