package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.PendingUser;
import com.example.plagiarism1.model.PendingValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PendingValidationRepository extends JpaRepository<PendingValidation, Long> {
    Optional<PendingValidation> findByPendingUser(PendingUser pendingUser);
    Optional<PendingValidation> findByCode(String code);
    @Modifying
    @Query("DELETE FROM PendingValidation v WHERE v.pendingUser.id = :userId")
    void deleteByPendingUser(@Param("userId") Long userId);
//    @Modifying
//    @Query("DELETE FROM PendingValidation v WHERE v.pendingUser.id = :userId")
//    void deleteByPendingUser(@Param("userId") Long userId);
}