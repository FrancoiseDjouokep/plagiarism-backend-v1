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

    public void enregistrer(Utilisateur utilisateur){
        Validation validation = new Validation();
        validation.setUtilisateur(utilisateur);
        Instant creation = Instant.now();
        validation.setCreation(creation);
        Instant expire = creation.plus(60,MINUTES);
 validation.setExpire(expire);
        Random random = new Random();
        int randomInteger = random.nextInt(999999);
        String code = String.format("%06d",randomInteger);

        validation.setCode(code);
        this.validationRepository.save(validation);
        this.notificationService.envoyer(validation);
    }
    public Validation lireEnFonctionDuCode(String code){
       return this.validationRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Votre code est invalide"));

    }
}
