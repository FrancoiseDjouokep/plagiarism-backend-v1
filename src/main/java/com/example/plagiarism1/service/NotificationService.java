package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Validation;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void envoyer(Validation validation) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        //mailMessage.setFrom("francoiseleslie@gmail.com");
        mailMessage.setTo(validation.getUtilisateur().getEmail());
        mailMessage.setSubject("Votre code d'activation");
        String texte = String.format("Bonjour %s, <br /> votre code d'activation est %s; A bientot",
                validation.getUtilisateur().getNom(),
                validation.getCode());
        mailMessage.setText(texte);
        javaMailSender.send(mailMessage);
    }

    /**
     * Send password reset email with code
     * @param validation Validation object containing the code and user info
     */
    public void envoyerResetPassword(Validation validation) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(validation.getUtilisateur().getEmail());
        mailMessage.setSubject("Réinitialisation de votre mot de passe");
        String texte = String.format("Bonjour %s, <br /> votre code de réinitialisation de mot de passe est %s; <br /> Ce code expirera dans 60 minutes. <br /> A bientot",
                validation.getUtilisateur().getNom(),
                validation.getCode());
        mailMessage.setText(texte);
        javaMailSender.send(mailMessage);
    }
}