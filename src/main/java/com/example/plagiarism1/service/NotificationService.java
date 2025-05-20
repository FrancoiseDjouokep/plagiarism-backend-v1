package com.example.plagiarism1.service;

import com.example.plagiarism1.model.PendingUser;
import com.example.plagiarism1.model.PendingValidation;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender javaMailSender;
    private final String adminEmail = "francoiseleslie05@gmail.com"; // À configurer dans application.properties

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // Méthode existante pour la validation email (Utilisateur)
    public void envoyer(Validation validation) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(validation.getUtilisateur().getEmail());
            mailMessage.setSubject("Votre code d'activation");

            String texte = String.format(
                    "Bonjour %s,\n\n" +
                            "Merci pour votre inscription. Votre code d'activation est: %s\n\n" +
                            "Pour activer votre compte, veuillez utiliser ce code sur notre site.\n\n" +
                            "Ce code expirera dans 60 minutes.\n\n" +
                            "Après activation, un administrateur devra approuver votre compte.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe de Plagiarism Detector",
                    validation.getUtilisateur().getNom(),
                    validation.getCode());

            mailMessage.setText(texte);
            javaMailSender.send(mailMessage);
            logger.info("Email d'activation envoyé à: {}", validation.getUtilisateur().getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email d'activation", e);
        }
    }

    @Async
    public void envoyerPendingUserValidation(PendingValidation validation) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(validation.getPendingUser().getEmail());
            helper.setSubject("Vérification de votre email");


            String texte = String.format(
                    "Bonjour %s,\n\n" +
                            "Merci pour votre inscription. Votre code d'activation est: %s\n\n" +
                            "Pour activer votre compte, veuillez utiliser ce code sur notre site.\n\n" +
                            "Ce code expirera dans 60 minutes.\n\n" +
                            "Après activation, un administrateur devra approuver votre compte.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe de Plagiarism Detector",
                    validation.getPendingUser().getNom(),
                    validation.getCode());

            helper.setText(texte);

            // Envoi avec vérification
            javaMailSender.send(message);
            logger.info("Email envoyé à {}", validation.getPendingUser().getEmail());

        } catch (MailAuthenticationException e) {
            logger.error("ERREUR SMTP: Problème d'authentification", e);
           // throw new NotificationException("Erreur d'authentification SMTP");
        } catch (Exception e) {
            logger.error("ERREUR SMTP: ", e);
           // throw new NotificationException("Échec d'envoi d'email");
        }
    }

    // Nouvelle méthode pour notifier l'admin d'une nouvelle demande d'inscription
    public void notifierAdminNouvelleInscription(PendingUser pendingUser) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(adminEmail);
            mailMessage.setSubject("Nouvelle inscription à approuver");

            String texte = String.format(
                    "Bonjour administrateur,\n\n" +
                            "Un nouvel utilisateur a soumis une demande d'inscription:\n\n" +
                            "Nom: %s %s\n" +
                            "Email: %s\n\n" +
                            "Veuillez vous connecter à l'interface d'administration pour approuver ou rejeter cette demande.\n\n" +
                            "Cordialement,\n" +
                            "Système de notification",
                    pendingUser.getPrenom(),
                    pendingUser.getNom(),
                    pendingUser.getEmail());

            mailMessage.setText(texte);
            javaMailSender.send(mailMessage);
            logger.info("Notification admin envoyée pour nouvelle inscription: {}", pendingUser.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification admin", e);
        }
    }

    /**
     * Envoie un email de réinitialisation de mot de passe à l'utilisateur
     * @param validation Objet validation contenant l'utilisateur et le code
     */
    public void envoyerResetPassword(Validation validation) {
        String resetUrl = "http://localhost:3000/password";
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(validation.getUtilisateur().getEmail());
            mailMessage.setSubject("Réinitialisation de votre mot de passe");
            String texte = String.format(
                    "Bonjour %s, \n\n" +
                            "Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte. \n\n" +
                            "Votre code de réinitialisation est: %s \n\n" +
                            "Pour réinitialiser votre mot de passe, veuillez utiliser ce code sur notre site:" + resetUrl + ". \n\n" +
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
    // Dans NotificationService.java
    public void envoyerApprovalNotification(Utilisateur utilisateur) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(utilisateur.getEmail());
            mailMessage.setSubject("Votre compte a été approuvé");

            String texte = String.format(
                    "Bonjour %s %s,\n\n" +
                            "Votre compte a été approuvé par l'administrateur.\n\n" +
                            "Vous pouvez maintenant vous connecter à votre compte en utilisant votre email et mot de passe.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe de Plagiarism Detector",
                    utilisateur.getPrenom(),
                    utilisateur.getNom()
            );

            mailMessage.setText(texte);
            javaMailSender.send(mailMessage);
            logger.info("Notification d'approbation envoyée à: {}", utilisateur.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification d'approbation", e);
        }
    }
    public void envoyerRejetNotification(PendingUser pendingUser, String raison) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(pendingUser.getEmail());
            mailMessage.setSubject("Votre inscription a été rejetée");

            String texte = String.format(
                    "Bonjour %s,\n\n" +
                            "Votre demande d'inscription a été rejetée pour la raison suivante:\n" +
                            "%s\n\n" +
                            "Cordialement,\n" +
                            "L'équipe de Plagiarism Detector",
                    pendingUser.getPrenom(),
                    raison
            );

            mailMessage.setText(texte);
            javaMailSender.send(mailMessage);
            logger.info("Notification de rejet envoyée à: {}", pendingUser.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de rejet", e);
        }
    }

}