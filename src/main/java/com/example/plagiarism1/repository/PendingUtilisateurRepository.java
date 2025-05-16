package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.PendingUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingUtilisateurRepository extends JpaRepository<PendingUtilisateur, Long> {
    Optional<PendingUtilisateur> findByEmail(String email);
}

