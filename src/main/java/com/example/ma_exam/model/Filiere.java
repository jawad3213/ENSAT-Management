package com.example.ma_exam.model;

public class Filiere {
    private int id;
    private String code;
    private String nom;
    private String description;
    private int nbEleves; // Display only (COUNT of students, filled by JOIN)

    public Filiere() {}

    public Filiere(int id, String code, String nom, String description) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.description = description;
    }

    public Filiere(String code, String nom, String description) {
        this.code = code;
        this.nom = nom;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getNbEleves() { return nbEleves; }
    public void setNbEleves(int nbEleves) { this.nbEleves = nbEleves; }

    @Override
    public String toString() {
        return nom + " (" + code + ")";
    }
}
