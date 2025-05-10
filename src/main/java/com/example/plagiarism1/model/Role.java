package com.example.plagiarism1.model;

import com.example.plagiarism1.TypeDeRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;
    @Enumerated(EnumType.STRING)
    private TypeDeRole libelle;
    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<Utilisateur> utilisateurs;

    public Role(long id, TypeDeRole libelle, List<Utilisateur> utilisateurs) {
        this.id = id;
        this.libelle = libelle;
        this.utilisateurs = utilisateurs;
    }

    public Role(){

    }

    public static Role.Builder builder() {
        return new Role.Builder();
    }

    public static class Builder {
        private long id ;
        private TypeDeRole libelle;
        private List<Utilisateur> utilisateurs;


        public Role.Builder id(long id) {
            this.id = id;
            return this;
        }

        public Role.Builder libelle(TypeDeRole libelle) {
            this.libelle = libelle;
            return this;
        }

        public Role.Builder utilisateur(List<Utilisateur> utilisateurs) {
            this.utilisateurs = utilisateurs;
            return this;
        }
        public Role build() {
            return new Role(id, libelle, utilisateurs);
        }
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TypeDeRole getLibelle() {
        return libelle;
    }

    public void setLibelle(TypeDeRole libelle) {
        this.libelle = libelle;
    }
    public List<Utilisateur> getUtilisateurs() {
        return utilisateurs;
    }

    public void setUtilisateurs(List<Utilisateur> utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

}
