package com.example.plagiarism1.service;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.model.Role;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import com.example.plagiarism1.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class UtilisateurService implements UserDetailsService {
    private UtilisateurRepository utilisateurRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private ValidationService validationService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, BCryptPasswordEncoder passwordEncoder, ValidationService validationService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
    }

    public void inscription(Utilisateur utilisateur){
        if(!utilisateur.getEmail().contains("@")){
            throw new RuntimeException("Votre email n'est pas valide");
        }
        if(!utilisateur.getEmail().contains(".")){
            throw new RuntimeException("Votre email n'est pas valide");
        }
        Optional<Utilisateur> utilisateurOptional= this.utilisateurRepository.findByEmail(utilisateur.getEmail());
        if(utilisateurOptional.isPresent()){
            throw new RuntimeException("Votre email est deja utiliser");
        }
        String mdpCrypte =  this.passwordEncoder.encode(utilisateur.getPassword());
        Role roleUtilisateur = new Role();
        roleUtilisateur.setLibelle(String.valueOf(TypeDeRole.UTILISATEUR));
        utilisateur.setRole(roleUtilisateur);
        utilisateur.setPassword(mdpCrypte);

        utilisateur= this.utilisateurRepository.save(utilisateur);
        this.validationService.enregistrer(utilisateur);
    }

    public void activation(Map<String, String> activation) {
         Validation validation = this.validationService.lireEnFonctionDuCode(activation.get("code"));
         if(Instant.now().isAfter(validation.getExpire())){
             throw new RuntimeException("Votre delai est expirer");
         }
        Utilisateur utilisateurActiver= this.utilisateurRepository.findById(validation.getUtilisateur().getId()).orElseThrow(() -> new RuntimeException("Utilisateur inconnu"));
         utilisateurActiver.setActif(true);
         this.utilisateurRepository.save(utilisateurActiver);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.utilisateurRepository
                .findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond a cette identifiant"));

    }
}
