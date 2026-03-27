package com.example.ma_exam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class to manage JDBC connection to a local SQLite database.
 * No server, no internet, no installation required — just a .db file on disk.
 */
public class DBConnection {
    private static final String DB_PATH = System.getProperty("user.home") + "/gestion_academique.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    private static Connection connection = null;

    private DBConnection() {
        // Private constructor for Singleton pattern
    }

    /**
     * Returns the singleton instance of the database connection.
     * On first call, creates the database file and all tables automatically.
     * @return Connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);

                // Enable foreign keys (SQLite disables them by default)
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }

                // Auto-create tables on first launch
                initializeDatabase();

                System.out.println("Connection successful to SQLite database at: " + DB_PATH);
            }
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Creates all tables if they don't exist yet.
     * Called automatically on first connection.
     */
    private static void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {

            // Table: Filiere
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS filiere (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    code TEXT NOT NULL UNIQUE," +
                "    nom TEXT NOT NULL," +
                "    description TEXT" +
                ")"
            );

            // Table: Cours
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS cours (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    code TEXT NOT NULL UNIQUE," +
                "    intitule TEXT NOT NULL" +
                ")"
            );

            // Table: Eleve
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS eleve (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    matricule TEXT NOT NULL UNIQUE," +
                "    nom TEXT NOT NULL," +
                "    prenom TEXT NOT NULL," +
                "    email TEXT NOT NULL UNIQUE," +
                "    filiere_id INTEGER," +
                "    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'SUSPENDED'))," +
                "    FOREIGN KEY (filiere_id) REFERENCES filiere(id) ON DELETE RESTRICT" +
                ")"
            );

            // Table: DossierAdministratif
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS dossier_administratif (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    numero_inscription TEXT NOT NULL UNIQUE," +
                "    date_creation TEXT NOT NULL," +
                "    eleve_id INTEGER UNIQUE NOT NULL," +
                "    FOREIGN KEY (eleve_id) REFERENCES eleve(id) ON DELETE CASCADE" +
                ")"
            );

            // Table: filiere_cours (association)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS filiere_cours (" +
                "    filiere_id INTEGER," +
                "    cours_id INTEGER," +
                "    PRIMARY KEY (filiere_id, cours_id)," +
                "    FOREIGN KEY (filiere_id) REFERENCES filiere(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE" +
                ")"
            );

            // Table: eleve_cours (association)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS eleve_cours (" +
                "    eleve_id INTEGER," +
                "    cours_id INTEGER," +
                "    PRIMARY KEY (eleve_id, cours_id)," +
                "    FOREIGN KEY (eleve_id) REFERENCES eleve(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE" +
                ")"
            );

            System.out.println("Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Closes the database connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed.");
            } catch (SQLException e) {
                System.err.println("Error while closing connection: " + e.getMessage());
            }
        }
    }
}
