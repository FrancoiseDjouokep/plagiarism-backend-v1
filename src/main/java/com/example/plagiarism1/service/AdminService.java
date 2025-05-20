package com.example.plagiarism1.service;

import com.example.plagiarism1.model.PendingUser;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.PendingUserRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final PendingUserRepository pendingUserRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;

    public AdminService(PendingUserRepository pendingUserRepository,
                        UtilisateurRepository utilisateurRepository,
                        NotificationService notificationService) {
        this.pendingUserRepository = pendingUserRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
    }

    public void validerInscription(Long pendingUserId) {
        PendingUser pendingUser = pendingUserRepository.findById(pendingUserId)
                .orElseThrow(() -> new RuntimeException("Demande d'inscription introuvable"));

        if (!pendingUser.isEmailVerified()) {
            throw new RuntimeException("Email non vérifié");
        }

        // Conversion vers Utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(pendingUser.getNom());
        utilisateur.setPrenom(pendingUser.getPrenom());
        utilisateur.setEmail(pendingUser.getEmail());
        utilisateur.setPassword(pendingUser.getPassword());
        utilisateur.setRole(pendingUser.getRole());
        utilisateur.setActif(true);

        utilisateurRepository.save(utilisateur);
        pendingUserRepository.delete(pendingUser);

        notificationService.envoyerApprovalNotification(utilisateur);
        logger.info("Inscription validée pour {} (ID: {})", pendingUser.getEmail(), pendingUserId);
    }

    public void rejeterInscription(Long pendingUserId, String raison) {
        PendingUser pendingUser = pendingUserRepository.findById(pendingUserId)
                .orElseThrow(() -> new RuntimeException("Demande d'inscription introuvable"));

        notificationService.envoyerRejetNotification(pendingUser, raison);
        pendingUserRepository.delete(pendingUser);

        logger.info("Inscription rejetée pour {} (ID: {}). Raison: {}",
                pendingUser.getEmail(), pendingUserId, raison);
    }
}