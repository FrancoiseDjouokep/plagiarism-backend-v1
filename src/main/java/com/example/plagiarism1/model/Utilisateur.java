package com.example.plagiarism1.model;

import jakarta.persistence.*;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Entité représentant un utilisateur dans le système
 * Implémente UserDetails pour l'intégration avec Spring Security
 */

@Entity
@Table(name = "Utilisateur")
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private boolean actif = false;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "role_id")
    private Role role;

    /**
     * Constructeur par défaut requis par JPA
     */
    public Utilisateur() {}

    /**
     * Constructeur complet
     */
    public Utilisateur(String nom, String prenom, String password, String email, boolean actif, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.email = email;
        this.actif = actif;
        this.role = role;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public static Utilisateur.Builder builder() {
//        return new Utilisateur.Builder();
//    }
//
//    public static class Builder {
//        private long id;
//        private String nom;
//        private String prenom;
//        private String password;
//        private String email;
//        private boolean actif = false;
//        private Role role;
//
//        public Utilisateur.Builder id(long id) {
//            this.id = id;
//            return this;
//        }
//
//        public Utilisateur.Builder nom(String nom) {
//            this.nom = nom;
//            return this;
//        }
//
//
//        public Utilisateur.Builder prenom(String prenom) {
//            this.prenom = prenom;
//            return this;
//        }
//
//        public Utilisateur.Builder password(String password) {
//            this.password = password;
//            return this;
//        }
//
//        public Utilisateur.Builder email(String email) {
//            this.email = email;
//            return this;
//        }
//
//
//        public Utilisateur.Builder actif(boolean actif) {
//            this.actif = actif;
//            return this;
//        }
//
//
//        public Utilisateur.Builder role(Role role) {
//            this.role = role;
//            return this;
//        }
//
//        public Utilisateur build() {
//            return new Utilisateur(id, nom, prenom, password, email, actif, role);
//        }
//    }
    // Méthodes de UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (this.role != null ? this.role.getLibelle() : "USER"))
        );
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        // Utilisation de l'email comme identifiant unique pour la connexion
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.actif;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.actif;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.actif;
    }

    @Override
    public boolean isEnabled() {
        return this.actif;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", actif=" + actif +
                ", role=" + (role != null ? role.getLibelle() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Utilisateur that = (Utilisateur) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Transient
    public boolean isNew() {
        return id == null || id == 0L;
    }

}
