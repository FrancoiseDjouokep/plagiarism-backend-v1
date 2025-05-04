package com.example.plagiarism1.repository;

import com.example.plagiarism1.TypeDeRole;
import com.example.plagiarism1.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByLibelle(TypeDeRole libelle);
}

