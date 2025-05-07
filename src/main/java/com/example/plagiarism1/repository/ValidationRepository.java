package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.Utilisateur;
import com.example.plagiarism1.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ValidationRepository extends JpaRepository<Validation, Long> {

    Optional<Validation> findByCode(String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM Validation v WHERE v.utilisateur.id = :utilisateurId")
    void deleteByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
    Optional<Validation> findByUtilisateur(Utilisateur utilisateur);
}
