package com.example.plagiarism1.service;

import com.example.plagiarism1.AuthProvider;
import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.model.Role;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.RoleRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    public CustomOAuth2UserService(UtilisateurRepository utilisateurRepository, RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;

        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("loaduser appeler");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String providerName = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        AuthProvider provider = AuthProvider.valueOf(providerName);

        String email = oAuth2User.getAttribute("email");
        if (email == null && provider == AuthProvider.GITHUB) {
            email = oAuth2User.getAttribute("login") + "@github.com"; // fallback GitHub
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("Email non trouvé via OAuth2");
        }

        String finalEmail = email;
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElseGet(() -> {
            Utilisateur nouveau = new Utilisateur();
            nouveau.setEmail(finalEmail);
            nouveau.setNom(oAuth2User.getAttribute("name") != null ? oAuth2User.getAttribute("name") : "OAuth_" + providerName);
            Role defaultRole = roleRepository.findByLibelle(TypeDeRole.ETUDIANT)
                    .orElseThrow(() -> {
                        System.out.println("❌ Le rôle ETUDIANT n'a pas été trouvé !");
                        return new RuntimeException("Default role not found");
                    });
            nouveau.setRole(defaultRole); // ou logique personnalisée
            nouveau.setProvider(provider);
            return utilisateurRepository.save(nouveau);
        });

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole())),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}
