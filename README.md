# Système de Gestion Académique - ENSAT

Ce projet est une application de bureau performante et moderne conçue pour la gestion administrative d'un établissement d'enseignement. Elle permet de gérer les étudiants, les filières, les cours et les dossiers administratifs au sein d'un environnement fluide et sécurisé.

## 🏛️ Architecture du Projet

L'application suit une architecture **Layered MVC (Model-View-Controller)** pour garantir une séparation claire des responsabilités :

*   **Model** : Contient les objets POJO (`Eleve`, `Filiere`, `Cours`, `DossierAdministratif`) représentant les entités de données.
*   **View (FXML)** : Interfaces graphiques définies en XML (JavaFX), stylisées avec du CSS personnalisé pour un rendu premium et moderne.
*   **Controller** : Gère la logique d'interaction entre l'utilisateur (Vue) et les données (DAO).
*   **DAO (Data Access Object)** : Couche d'accès aux données utilisant JDBC pour communiquer avec la base de données PostgreSQL.
*   **Util** : Classes utilitaires, notamment pour la gestion de la connexion à la base de données (`DBConnection`).

## � Structure Détaillée du Projet

### 🧩 Modèles (`com.example.ma_exam.model`)
Représentent les entités de données du système.
*   **`Eleve.java`** : Définit les attributs d'un étudiant (matricule, nom, email, etc.) .
*   **`Filiere.java`** : Représente une filière académique (Génie Informatique, etc.).
*   **`Cours.java`** : Modélise les matières enseignées.
*   **`DossierAdministratif.java`** : Gère les informations d'inscription liées à un élève spécifique.

### 🗄️ Accès aux Données (`com.example.ma_exam.dao`)
Gèrent toutes les interactions directes avec la base de données PostgreSQL via JDBC.
*   **`EleveDAO.java`** : Opérations CRUD pour les élèves, incluant les jointures complexes (nom de filière) et la gestion des inscriptions.
*   **`FiliereDAO.java`** : Gestion des filières et statistiques de comptage pour le dashboard.
*   **`CoursDAO.java`** : Gestion des cours et filtrage par filière pour les inscriptions.
*   **`DossierDAO.java`** : Persistance des dossiers administratifs et récupération avec informations complètes de l'élève.

### 🎮 Contrôleurs (`com.example.ma_exam.controller`)
Assurent le lien entre la vue et les données.
*   **`LoginController.java`** : Gère l'authentification et l'accès initial au système.
*   **`DashboardController.java`** : Orchestre la navigation principale (Menu Sidebar) et la déconnexion.
*   **`StudentController.java`** : Contrôle la gestion des élèves et déclenche la création automatique des dossiers.
*   **`FiliereController.java`** & **`CoursController.java`** : Gèrent les interfaces CRUD respectives.
*   **`DossierController.java`** : Gère l'affichage détaillé de la table des dossiers administratifs.
*   **`StatsController.java`** : Pilote le tableau de bord avec les cartes de statistiques et les mini-tables d'aperçu rapide.

### 🎨 Vues (`src/main/resources/com/example/ma_exam/view`)
Définissent l'aspect visuel de l'application.
*   **`login-view.fxml`** : Interface de connexion stylisée avec le logo ENSAT.
*   **`dashboard.fxml`** : Structure maîtresse (Sidebar + Content Area).
*   **`dashboard-stats.fxml`** : Vue d'accueil avec les indicateurs clés de performance.
*   **`student-view.fxml`**, `filiere-view.fxml`, `cours-view.fxml`, `dossier-view.fxml` : Interfaces modulaires pour la gestion des données.
*   **`style.css`** : Feuille de style globale (Modern Dark Blue Theme).

## 🛠️ Choix Techniques

*   **Langage** : Java 17 (LTS) pour la robustesse et les performances.
*   **Interface Graphique** : **JavaFX 17** pour une UI moderne et fluide.
*   **Base de Données** : **PostgreSQL** (Types ENUM, Clés étrangères, Cascades).
*   **Persistance** : **JDBC** natif pour une maîtrise totale des requêtes SQL.
*   **Gestion de Projet** : **Maven** pour un cycle de build standardisé.

## 🚀 Difficultés Rencontrées

1.  **Gestion du Type ENUM PostgreSQL** : L'intégration du type `student_status` a nécessité l'usage de `CAST(? AS student_status)` dans JDBC.
2.  **Expérience Utilisateur (UX)** : Le défi de créer une interface "Premium" a été relevé grâce à une personnalisation CSS avancée.
3.  **Coordination Automatisée** : La création automatique d'un dossier administratif à chaque nouvel étudiant a demandé une gestion rigoureuse des callbacks DAO.

## 👤 Auteur
**EL HAIL JAOUAD**
