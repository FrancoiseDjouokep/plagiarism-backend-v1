package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.Analysis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

}
