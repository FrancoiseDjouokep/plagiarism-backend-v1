package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import com.example.plagiarism1.repository.ValidationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
public class ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final ValidationRepository validationRepository;
    private final NotificationService notificationService;

    public ValidationService(ValidationRepository validationRepository, NotificationService notificationService) {
        this.validationRepository = validationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Crée un nouveau code de validation pour un utilisateur
     * Si une validation existe déjà pour cet utilisateur, la remplace par une nouvelle
     * @param utilisateur L'utilisateur pour lequel créer le code
     */
    @Transactional
    public void enregistrer(Utilisateur utilisateur) {
        logger.debug("Création d'un code de validation pour l'utilisateur: {}", utilisateur.getEmail());

        // Rechercher d'abord s'il existe déjà une validation pour cet utilisateur
        Optional<Validation> existingValidation = validationRepository.findByUtilisateur(utilisateur);

        // Créer une nouvelle validation (qu'on va soit mettre à jour soit créer)
        Validation validation;

        if (existingValidation.isPresent()) {
            // Si une validation existe déjà, réutiliser son ID pour la mise à jour
            validation = existingValidation.get();
            logger.debug("Mise à jour d'un code de validation existant pour l'utilisateur: {}", utilisateur.getEmail());
        } else {
            // Sinon créer une nouvelle validation
            validation = new Validation();
            validation.setUtilisateur(utilisateur);
            logger.debug("Création d'un nouveau code de validation pour l'utilisateur: {}", utilisateur.getEmail());
        }

        // Mise à jour des champs
        Instant creation = Instant.now();
        validation.setCreation(creation);

        Instant expire = creation.plus(60, MINUTES);
        validation.setExpire(expire);

        // Générer un nouveau code
        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d", randomInteger);
        validation.setCode(code);

        // Sauvegarder la validation
        validationRepository.save(validation);
        logger.info("Code de validation créé avec succès pour l'utilisateur: {}", utilisateur.getEmail());

        // Envoyer l'email approprié
        if (utilisateur.isActif()) {
            this.notificationService.envoyerResetPassword(validation);
            logger.info("Email de réinitialisation de mot de passe envoyé à: {}", utilisateur.getEmail());
        } else {
            this.notificationService.envoyer(validation);
            logger.info("Email d'activation envoyé à: {}", utilisateur.getEmail());
        }
    }

    /**
     * Récupère une validation par son code
     * @param code Le code de validation
     * @return La validation
     * @throws RuntimeException Si le code est invalide
     */
    public Validation lireEnFonctionDuCode(String code) {
        return this.validationRepository
                .findByCode(code)
                .orElseThrow(() -> new RuntimeException("Votre code est invalide"));
    }

    /**
     * Supprime une validation
     * @param validation La validation à supprimer
     */
    public void supprimer(Validation validation) {
        this.validationRepository.delete(validation);
        logger.debug("Validation supprimée pour l'utilisateur: {}",
                validation.getUtilisateur() != null ? validation.getUtilisateur().getEmail() : "inconnu");
    }
}