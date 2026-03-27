-- ==========================================================
-- SCRIPT DE CRÉATION DE LA BASE DE DONNÉES (POSTGRESQL)
-- Projet: Gestion Académique (JavaFX + JDBC)
-- ==========================================================

-- 1. Note: PostgreSQL typically requires you to create the database manually 
-- or use \c to switch to it. Ensure the database 'gestion_academique' exists.

-- 2. Suppression des tables si elles existent (ordre respectant les clés étrangères)
DROP TABLE IF EXISTS eleve_cours CASCADE;
DROP TABLE IF EXISTS filiere_cours CASCADE;
DROP TABLE IF EXISTS dossier_administratif CASCADE;
DROP TABLE IF EXISTS eleve CASCADE;
DROP TABLE IF EXISTS cours CASCADE;
DROP TABLE IF EXISTS filiere CASCADE;

-- Suppression du type ENUM s'il existe
DROP TYPE IF EXISTS student_status;

-- 3. Création du type ENUM pour le statut de l'élève
CREATE TYPE student_status AS ENUM ('ACTIVE', 'SUSPENDED');

-- 4. Table: Filiere
CREATE TABLE filiere (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    description TEXT
);

-- 5. Table: Cours
CREATE TABLE cours (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    intitule VARCHAR(150) NOT NULL
);

-- 6. Table: Eleve
CREATE TABLE eleve (
    id SERIAL PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    filiere_id INT,
    status student_status DEFAULT 'ACTIVE',
    CONSTRAINT fk_eleve_filiere FOREIGN KEY (filiere_id) REFERENCES filiere(id) ON DELETE RESTRICT
);

-- 7. Table: DossierAdministratif
CREATE TABLE dossier_administratif (
    id SERIAL PRIMARY KEY,
    numero_inscription VARCHAR(50) NOT NULL UNIQUE,
    date_creation DATE NOT NULL,
    eleve_id INT UNIQUE NOT NULL,
    CONSTRAINT fk_dossier_eleve FOREIGN KEY (eleve_id) REFERENCES eleve(id) ON DELETE CASCADE
);

-- 8. Table d'association: filiere_cours
CREATE TABLE filiere_cours (
    filiere_id INT,
    cours_id INT,
    PRIMARY KEY (filiere_id, cours_id),
    CONSTRAINT fk_fc_filiere FOREIGN KEY (filiere_id) REFERENCES filiere(id) ON DELETE CASCADE,
    CONSTRAINT fk_fc_cours FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);

-- 9. Table d'association: eleve_cours
CREATE TABLE eleve_cours (
    eleve_id INT,
    cours_id INT,
    PRIMARY KEY (eleve_id, cours_id),
    CONSTRAINT fk_ec_eleve FOREIGN KEY (eleve_id) REFERENCES eleve(id) ON DELETE CASCADE,
    CONSTRAINT fk_ec_cours FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);
