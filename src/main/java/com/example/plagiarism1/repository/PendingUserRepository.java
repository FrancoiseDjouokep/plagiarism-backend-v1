package com.example.plagiarism1.repository;

import com.example.plagiarism1.model.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {
    Optional<PendingUser> findByEmail(String email);
    List<PendingUser> findAllByEmailVerifiedTrue();
}

