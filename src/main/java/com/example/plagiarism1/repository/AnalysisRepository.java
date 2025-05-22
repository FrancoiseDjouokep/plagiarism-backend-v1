package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.Analysis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findBySourceDocumentId(Long sourceDocumentId);

    List<Analysis> findAllByUtilisateurEmail(String email);

}
