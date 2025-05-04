package com.example.plagiarism1.util;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.controller.UtilisateurController;
import com.example.plagiarism1.model.Role;
import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.repository.RoleRepository;
import com.example.plagiarism1.repository.UtilisateurRepository;
import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@SpringBootApplication
public class PlagiarismUserApp implements CommandLineRunner {

    private static final Logger log =LoggerFactory.getLogger(PlagiarismUserApp.class);

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PlagiarismUserApp(UtilisateurRepository utilisateurRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(PlagiarismUserApp.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            // Ensure ADMIN role exists
            Role adminRole = roleRepository.findByLibelle(TypeDeRole.ADMIN)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setLibelle(TypeDeRole.ADMIN);
                        return roleRepository.save(role);
                    });

            // Check and create admin user
            String adminEmail = "francoiseleslie05@gmail.com";
            if (!utilisateurRepository.existsByEmail(adminEmail)) {
                Utilisateur admin = new Utilisateur();
                        admin.setNom("Francoise");
                        admin.setPrenom("Leslie");
                        admin.setPassword(passwordEncoder.encode("Leslie05%"));
                        admin.setEmail(adminEmail);
                        admin.setActif(true);
                        admin.setRole(adminRole);

                log.info("Saving admin user: {}", admin);


                utilisateurRepository.save(admin);
                log.info("Admin user created successfully");
            } else {
                log.info("Admin user already exists");
            }
        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }
}