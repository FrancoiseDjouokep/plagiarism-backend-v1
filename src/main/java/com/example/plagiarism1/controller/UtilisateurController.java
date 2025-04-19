package com.example.plagiarism1.controller;

import ch.qos.logback.classic.Logger;
import com.example.plagiarism1.AuthentificationDTO;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.service.JwtService;
import com.example.plagiarism1.service.UtilisateurService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
public class UtilisateurController {
    private UtilisateurService utilisateurService;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    public UtilisateurController(UtilisateurService utilisateurService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping(path = "inscription")
    public void inscription(@RequestBody Utilisateur utilisateur){
        System.out.println("Inscription appelée !");
        this.utilisateurService.inscription(utilisateur);
    }
    @PostMapping(path = "activation")
    public void activation(@RequestBody Map<String, String> activation){

        this.utilisateurService.activation(activation);
    }
    @PostMapping(path = "connexion")
    public  Map<String, String> connexion(@RequestBody AuthentificationDTO authentificationDTO){
       final Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authentificationDTO.username(), authentificationDTO.password()));
       if(authenticate.isAuthenticated()){
           return  this.jwtService.generate(authentificationDTO.username());
       }
        return null;
    }
}
