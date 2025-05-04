package com.example.plagiarism1.model;

import com.example.plagiarism1.TypeDeRole;
import jakarta.persistence.*;

@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;
    @Enumerated(EnumType.STRING)
    private TypeDeRole libelle;

    public Role(long id, TypeDeRole libelle) {
        this.id = id;
        this.libelle = libelle;
    }

    public Role(){

    }

    public static Role.Builder builder() {
        return new Role.Builder();
    }

    public static class Builder {
        private long id ;
        private TypeDeRole libelle;

        public Role.Builder id(long id) {
            this.id = id;
            return this;
        }

        public Role.Builder libelle(TypeDeRole libelle) {
            this.libelle = libelle;
            return this;
        }
        public Role build() {
            return new Role(id, libelle);
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
}
