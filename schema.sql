-- ==========================================================
-- SCRIPT DE CRÉATION DE LA BASE DE DONNÉES (SQLITE)
-- Projet: Gestion Académique (JavaFX + JDBC)
-- Note: Tables are auto-created by DBConnection.java
-- This file is kept for documentation purposes.
-- ==========================================================

-- 1. Suppression des tables si elles existent
DROP TABLE IF EXISTS eleve_cours;
DROP TABLE IF EXISTS filiere_cours;
DROP TABLE IF EXISTS dossier_administratif;
DROP TABLE IF EXISTS eleve;
DROP TABLE IF EXISTS cours;
DROP TABLE IF EXISTS filiere;

-- 2. Table: Filiere
CREATE TABLE IF NOT EXISTS filiere (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    nom TEXT NOT NULL,
    description TEXT
);

-- 3. Table: Cours
CREATE TABLE IF NOT EXISTS cours (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    intitule TEXT NOT NULL
);

-- 4. Table: Eleve
CREATE TABLE IF NOT EXISTS eleve (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    matricule TEXT NOT NULL UNIQUE,
    nom TEXT NOT NULL,
    prenom TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    filiere_id INTEGER,
    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'SUSPENDED')),
    FOREIGN KEY (filiere_id) REFERENCES filiere(id) ON DELETE RESTRICT
);

-- 5. Table: DossierAdministratif
CREATE TABLE IF NOT EXISTS dossier_administratif (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_inscription TEXT NOT NULL UNIQUE,
    date_creation TEXT NOT NULL,
    eleve_id INTEGER UNIQUE NOT NULL,
    FOREIGN KEY (eleve_id) REFERENCES eleve(id) ON DELETE CASCADE
);

-- 6. Table d'association: filiere_cours
CREATE TABLE IF NOT EXISTS filiere_cours (
    filiere_id INTEGER,
    cours_id INTEGER,
    PRIMARY KEY (filiere_id, cours_id),
    FOREIGN KEY (filiere_id) REFERENCES filiere(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);

-- 7. Table d'association: eleve_cours
CREATE TABLE IF NOT EXISTS eleve_cours (
    eleve_id INTEGER,
    cours_id INTEGER,
    PRIMARY KEY (eleve_id, cours_id),
    FOREIGN KEY (eleve_id) REFERENCES eleve(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);
