package com.example.ma_exam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class to manage JDBC connection to the database.
 */
public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/gestion_academique";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345"; // Change according to your PostgreSQL setup

    private static Connection connection = null;

    private DBConnection() {
        // Private constructor for Singleton pattern
    }

    /**
     * Returns the singleton instance of the database connection.
     * @return Connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Register PostgreSQL Driver
                Class.forName("org.postgresql.Driver");
                
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connection successful to the database.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
        return connection;
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
