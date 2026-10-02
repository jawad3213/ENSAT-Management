package com.example.ma_exam.model;

import java.time.LocalDate;

public class DossierAdministratif {
    private int id;
    private String numeroInscription;
    private LocalDate dateCreation;
    private int eleveId;
    private String eleveNomComplet; // Display only (filled by JOIN with eleve)
    private String eleveMatricule;  // Display only (filled by JOIN with eleve)

    public DossierAdministratif() {}

    public DossierAdministratif(int id, String numeroInscription, LocalDate dateCreation, int eleveId) {
        this.id = id;
        this.numeroInscription = numeroInscription;
        this.dateCreation = dateCreation;
        this.eleveId = eleveId;
    }

    public DossierAdministratif(String numeroInscription, LocalDate dateCreation, int eleveId) {
        this.numeroInscription = numeroInscription;
        this.dateCreation = dateCreation;
        this.eleveId = eleveId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroInscription() { return numeroInscription; }
    public void setNumeroInscription(String numeroInscription) { this.numeroInscription = numeroInscription; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public int getEleveId() { return eleveId; }
    public void setEleveId(int eleveId) { this.eleveId = eleveId; }

    public String getEleveNomComplet() { return eleveNomComplet; }
    public void setEleveNomComplet(String eleveNomComplet) { this.eleveNomComplet = eleveNomComplet; }

    public String getEleveMatricule() { return eleveMatricule; }
    public void setEleveMatricule(String eleveMatricule) { this.eleveMatricule = eleveMatricule; }

    @Override
    public String toString() {
        return "Dossier N°: " + numeroInscription;
    }
}
