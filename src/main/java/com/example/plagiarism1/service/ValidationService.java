package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import com.example.plagiarism1.repository.ValidationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class ValidationService {
    private ValidationRepository validationRepository;
    private NotificationService notificationService;

    public ValidationService(ValidationRepository validationRepository, NotificationService notificationService) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
    }

    public void enregistrer(Utilisateur utilisateur) {
        // Delete any existing validations for this user to avoid multiple active codes
        this.validationRepository
                .findByUtilisateur(utilisateur)
                .ifPresent(validationRepository::delete);

        Validation validation = new Validation();
        validation.setUtilisateur(utilisateur);

        Instant creation = Instant.now();
        validation.setCreation(creation);

        Instant expire = creation.plus(60, MINUTES);
        validation.setExpire(expire);

        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);

        validation.setCode(code);
        this.validationRepository.save(validation);

        // Check if it's for password reset or account activation
        if (utilisateur.isActif()) {
            // For password reset
            this.notificationService.envoyerResetPassword(validation);
        } else {
            // For account activation
            this.notificationService.envoyer(validation);
        }
    }

    public Validation lireEnFonctionDuCode(String code) {
        return this.validationRepository
                .findByCode(code)
                .orElseThrow(() -> new RuntimeException("Votre code est invalide"));
    }

    public void supprimer(Validation validation) {
        this.validationRepository.delete(validation);
    }
}