package com.example.plagiarism1.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "pending_validations", uniqueConstraints = @UniqueConstraint(columnNames = {"pending_user_id"}))
public class PendingValidation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private Instant creation;
    private Instant expire;

    @OneToOne
    @JoinColumn(name = "pending_user_id")
    @JsonManagedReference
    private PendingUser pendingUser;

    public PendingValidation(Long id, String code, Instant creation, Instant expire, PendingUser pendingUser) {
        this.id = id;
        this.code = code;
        this.creation = creation;
        this.expire = expire;
        this.pendingUser = pendingUser;
    }

    public PendingValidation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getCreation() {
        return creation;
    }

    public void setCreation(Instant creation) {
        this.creation = creation;
    }

    public Instant getExpire() {
        return expire;
    }

    public void setExpire(Instant expire) {
        this.expire = expire;
    }

    public PendingUser getPendingUser() {
        return pendingUser;
    }

    public void setPendingUser(PendingUser pendingUser) {
        this.pendingUser = pendingUser;
    }
}