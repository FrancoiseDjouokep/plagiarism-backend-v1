package com.example.plagiarism1.service;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.model.PendingUser;
import com.example.plagiarism1.model.PendingValidation;
import com.example.plagiarism1.model.Role;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.JwtRepository;
import com.example.plagiarism1.repository.PendingUserRepository;
import com.example.plagiarism1.repository.RoleRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    public void registerPendingUser(PendingUser pendingUser) {
        if (pendingUser.getRole() == null) {
            // Récupérer le rôle existant au lieu d'en créer un nouveau
            Role defaultRole = roleRepository.findByLibelle(TypeDeRole.ETUDIANT)
                    .orElseThrow(() -> new RuntimeException("Rôle ETUDIANT introuvable"));
            pendingUser.setRole(defaultRole);
        } else {
            // Vérifier si le rôle existe déjà
            Role existingRole = roleRepository.findByLibelle(pendingUser.getRole().getLibelle())
                    .orElse(null);
            if (existingRole != null) {
                pendingUser.setRole(existingRole);
            }
        }
        // 1. Vérifier que l'email n'est pas null
        if (pendingUser.getEmail() == null) {
            throw new RuntimeException("L'email est obligatoire");
        }

        // 2. Validation du format email (maintenant sur le bon objet)
        if (!pendingUser.getEmail().matches("[^@]+@[^@]+\\.[^@]+")) {
            throw new RuntimeException("Email invalide");
        }

        // 3. Vérification email existant
        if (utilisateurRepository.findByEmail(pendingUser.getEmail()).isPresent() ||
                pendingUserRepository.findByEmail(pendingUser.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé ou en attente de validation");
        }

        // 4. Hashage du mot de passe
        pendingUser.setPassword(passwordEncoder.encode(pendingUser.getPassword()));

        // 5. Sauvegarde
        PendingUser savedUser = pendingUserRepository.save(pendingUser);
        PendingValidation validation = validationService.enregistrer(savedUser);
        // 6. Envoi des notifications
        validationService.enregistrer(pendingUser);
        notificationService.envoyerPendingUserValidation(validation);
        notificationService.notifierAdminNouvelleInscription(savedUser);
    }
}
