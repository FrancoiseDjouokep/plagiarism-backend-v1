package com.example.plagiarism1.model;


import jakarta.persistence.*;


@Entity
@Table(name = "jwt")
public class Jwt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;
    private String valeur;
    private boolean desactive;
    private boolean expire;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private RefreshToken refreshToken;
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE})
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    public Jwt(long id, String valeur, boolean desactive, boolean expire, RefreshToken refreshToken, Utilisateur utilisateur) {
        this.id = id;
        this.valeur = valeur;
        this.desactive = desactive;
        this.expire = expire;
        this.refreshToken = refreshToken;
        this.utilisateur = utilisateur;
    }
    public Jwt(){

    }
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long id;
        private String valeur;
        private boolean desactive;
        private boolean expire;
        private RefreshToken refreshToken;
        private Utilisateur utilisateur;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder valeur(String valeur) {
            this.valeur = valeur;
            return this;
        }


        public Builder desactive(boolean desactive) {
            this.desactive = desactive;
            return this;
        }

        public Builder expire(boolean expire) {
            this.expire = expire;
            return this;
        }
        public Builder refreshToken(RefreshToken refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder utilisateur(Utilisateur utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public Jwt build() {
            return new Jwt(id, valeur, desactive, expire, refreshToken, utilisateur);
        }
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDesactive() {
        return desactive;
    }

    public void setDesactive(boolean desactive) {
        this.desactive = desactive;
    }

    public boolean isExpire() {
        return expire;
    }

    public void setExpire(boolean expire) {
        this.expire = expire;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }
    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }


}
