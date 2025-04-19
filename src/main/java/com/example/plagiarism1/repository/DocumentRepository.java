package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTitleContaining (String title);
    List<Document> findByLanguage (String language);

}
