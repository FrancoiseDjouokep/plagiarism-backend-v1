package com.example.plagiarism1.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh-token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private boolean expire;
    private String valeur;
    private Instant creation;
    private Instant expiration;

    public RefreshToken() {
    }

    public RefreshToken(int id, boolean expire, String valeur, Instant creation, Instant expiration) {
        this.id = id;
        this.expire = expire;
        this.valeur = valeur;
        this.creation = creation;
        this.expiration = expiration;
    }

    public int getId() {
        return id;
    }

    public boolean isExpire() {
        return expire;
    }

    public String getValeur() {
        return valeur;
    }

    public Instant getCreation() {
        return creation;
    }

    public Instant getExpiration() {
        return expiration;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setExpire(boolean expire) {
        this.expire = expire;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public void setCreation(Instant creation) {
        this.creation = creation;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

    public static RefreshTokenBuilder builder() {
        return new RefreshTokenBuilder();
    }

    public static class RefreshTokenBuilder {
        private int id;
        private boolean expire;
        private String valeur;
        private Instant creation;
        private Instant expiration;

        RefreshTokenBuilder() {
        }

        public RefreshTokenBuilder id(int id) {
            this.id = id;
            return this;
        }

        public RefreshTokenBuilder expire(boolean expire) {
            this.expire = expire;
            return this;
        }

        public RefreshTokenBuilder valeur(String valeur) {
            this.valeur = valeur;
            return this;
        }

        public RefreshTokenBuilder creation(Instant creation) {
            this.creation = creation;
            return this;
        }

        public RefreshTokenBuilder expiration(Instant expiration) {
            this.expiration = expiration;
            return this;
        }

        public RefreshToken build() {
            return new RefreshToken(id, expire, valeur, creation, expiration);
        }
    }
}
