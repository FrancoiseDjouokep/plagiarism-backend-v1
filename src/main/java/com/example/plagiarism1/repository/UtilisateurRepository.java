package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.Document;
import com.example.plagiarism1.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
}
