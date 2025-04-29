package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Envoie un email d'activation à l'utilisateur
     * @param validation Objet validation contenant l'utilisateur et le code
     */
    public void envoyer(Validation validation) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(validation.getUtilisateur().getEmail());
            mailMessage.setSubject("Votre code d'activation");
            String texte = String.format(
                    "Bonjour %s, \n\n" +
                            "Merci pour votre inscription. Votre code d'activation est: %s \n\n" +
                            "Pour activer votre compte, veuillez utiliser ce code sur notre site. \n\n" +
                            "Ce code expirera dans 60 minutes. \n\n" +
                            "Cordialement,\n" +
                            "L'équipe de Plagiarism Detector",
                    validation.getUtilisateur().getNom(),
                    validation.getCode());
            mailMessage.setText(texte);

            logger.debug("Envoi d'email d'activation à: {}", validation.getUtilisateur().getEmail());
            javaMailSender.send(mailMessage);
            logger.info("Email d'activation envoyé avec succès à: {}", validation.getUtilisateur().getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email d'activation: {}", e.getMessage(), e);
        }
    }

    /**
     * Envoie un email de réinitialisation de mot de passe à l'utilisateur
     * @param validation Objet validation contenant l'utilisateur et le code
     */
    public void envoyerResetPassword(Validation validation) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(validation.getUtilisateur().getEmail());
            mailMessage.setSubject("Réinitialisation de votre mot de passe");
            String texte = String.format(
                    "Bonjour %s, \n\n" +
                            "Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte. \n\n" +
                            "Votre code de réinitialisation est: %s \n\n" +
                            "Pour réinitialiser votre mot de passe, veuillez utiliser ce code sur notre site. \n\n" +
                            "Ce code expirera dans 60 minutes. Si vous n'avez pas demandé cette réinitialisation, " +
                            "vous pouvez ignorer cet email. \n\n" +
                            "Cordialement,\n" +
                            "L'équipe de Plagiarism Detector",
                    validation.getUtilisateur().getNom(),
                    validation.getCode());
            mailMessage.setText(texte);

            logger.debug("Envoi d'email de réinitialisation à: {}", validation.getUtilisateur().getEmail());
            javaMailSender.send(mailMessage);
            logger.info("Email de réinitialisation envoyé avec succès à: {}", validation.getUtilisateur().getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage(), e);
        }
    }
}