package com.example.plagiarism1.service;

import com.example.plagiarism1.model.Analysis;
import com.example.plagiarism1.model.Validation;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

   private  JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void envoyer(Validation validation){
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
}
