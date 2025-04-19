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

    public Role(long id, String libelle) {
        this.id = id;
        this.libelle = TypeDeRole.valueOf(libelle);
    }

    public Role(){

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

    public void setLibelle(String libelle) {
        this.libelle = TypeDeRole.valueOf(libelle);
    }
}
