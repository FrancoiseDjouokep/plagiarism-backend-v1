package com.example.plagiarism1.repository;

import com.example.plagiarism1.dto.DocumentTitleDTO;
import com.example.plagiarism1.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByLanguage (String language);
    List<Document> findAllByIdNot(Long id);
    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Document> searchByTitle(@Param("title") String title);
    @Query("SELECT new com.example.plagiarism1.dto.DocumentTitleDTO(d.id, d.title) " +
            "FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT(:query, '%'))")
    List<DocumentTitleDTO> searchByTitlePrefix(@Param("query") String query);

}
