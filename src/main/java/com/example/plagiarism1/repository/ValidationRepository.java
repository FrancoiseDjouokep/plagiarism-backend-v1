package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRepository extends JpaRepository<Validation, Long> {

    Optional<Validation> findByCode(String code);

    Optional<Validation> findByUtilisateur(Utilisateur utilisateur);
}
