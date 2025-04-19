package com.example.plagiarism1.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "Utilisateur")
public class Utilisateur implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nom;
    private String password;
    private String email;
    private boolean actif = false;
    @OneToOne(cascade = CascadeType.ALL)
    private Role role;
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

  // plus "final"

    // ✅ Constructeur vide pour Jackson
    public Utilisateur() {}

    public Utilisateur(String password, boolean actif, String email, String nom, long id, Role role) {
        this.password = password;
        this.actif = actif;
        this.email = email;
        this.nom = nom;
        this.id = id;
        this.role = role;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.getLibelle()));
    }

    @Override public String getPassword() { return this.password; }
    @Override public String getUsername() { return this.nom; }
    @Override public boolean isAccountNonExpired() { return this.actif; }
    @Override public boolean isAccountNonLocked() { return this.actif; }
    @Override public boolean isCredentialsNonExpired() { return this.actif; }
    @Override public boolean isEnabled() { return this.actif; }


}
