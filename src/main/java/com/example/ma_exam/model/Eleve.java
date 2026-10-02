package com.example.ma_exam.model;

public class Eleve {
    public enum Status {
        ACTIVE("Actif"), SUSPENDED("Suspendu");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private int id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private Integer filiereId;
    private Status status;
    private String filiereNom; // Display only (filled by JOIN with filiere)

    public Eleve() {
        this.status = Status.ACTIVE;
    }

    public Eleve(int id, String matricule, String nom, String prenom, String email, Integer filiereId, Status status) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.filiereId = filiereId;
        this.status = status;
    }

    public Eleve(String matricule, String nom, String prenom, String email, Integer filiereId) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.filiereId = filiereId;
        this.status = Status.ACTIVE;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getFiliereId() { return filiereId; }
    public void setFiliereId(Integer filiereId) { this.filiereId = filiereId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getFiliereNom() { return filiereNom; }
    public void setFiliereNom(String filiereNom) { this.filiereNom = filiereNom; }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + matricule + ")";
    }
}
