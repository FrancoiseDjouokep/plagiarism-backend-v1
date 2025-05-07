package com.example.plagiarism1.service;

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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UtilisateurRepository utilisateurRepository,
                                   RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        System.out.println("instanciation de la classe customoauth2userservice: ");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        System.out.println("🚨 CustomOAuth2UserService.loadUser() appelé !");

        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Get email - this should always be present for Google OAuth
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found in OAuth2 response");
        }

        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        Utilisateur utilisateur;

        if (utilisateurOpt.isEmpty()) {
            System.out.println("Utilisateur inconnu, création en cours...");

//            // Handle name attributes more robustly
//            Map<String, Object> attributes = oAuth2User.getAttributes();
//            String familyName = oAuth2User.getAttribute("family_name");
//            String givenName = oAuth2User.getAttribute("given_name");
//
//            // Fallback to name if given/family names not available
//            if (givenName == null || familyName == null) {
//                String fullName = oAuth2User.getAttribute("name");
//                if (fullName != null) {
//                    String[] names = fullName.split(" ", 2);
//                    givenName = names[0];
//                    familyName = names.length > 1 ? names[1] : "";
//                }
//            }

            utilisateur = new Utilisateur();
            utilisateur.setEmail(email);
            utilisateur.setNom("User");
            utilisateur.setPrenom("Userl");
            utilisateur.setActif(true);

            // Assign default role
            Role defaultRole = roleRepository.findByLibelle(TypeDeRole.ETUDIANT)
                    .orElseThrow(() -> {
                        System.out.println("❌ Le rôle ETUDIANT n'a pas été trouvé !");
                        return new RuntimeException("Default role not found");
                    });

            utilisateur.setRole(defaultRole);

            try {
                utilisateurRepository.save(utilisateur);
                System.out.println("✅ Nouvel utilisateur enregistré !");
            } catch (Exception e) {
                System.out.println("❌ Erreur lors de l'enregistrement de l'utilisateur : " + e.getMessage());
                throw new OAuth2AuthenticationException("Could not save new user");
            }
        } else {
            utilisateur = utilisateurOpt.get();
            System.out.println("🔄 Utilisateur déjà existant : " + utilisateur.getEmail());
        }

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(utilisateur.getRole().getLibelle().name())
        );

        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email"  // This is the name attribute key
        );
    }
}