package com.example.ma_exam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class to manage JDBC connection to the database.
 * The connection is shared: callers must NOT close it (use closeConnection() on shutdown).
 * Credentials are read from AppConfig (config.properties or environment variables).
 */
public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/gestion_academique";
    private static final String DEFAULT_USER = "postgres";

    private static Connection connection = null;

    /**
     * Work executed inside a transaction on the shared connection.
     */
    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute(Connection conn) throws SQLException, BusinessRuleException;
    }

    private DBConnection() {
        // Private constructor for Singleton pattern
    }

    /**
     * Returns the shared database connection, (re)opening it if needed.
     * @return Connection
     * @throws SQLException if the driver is missing or the database is unreachable
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || !connection.isValid(2)) {
            closeConnection();
            try {
                // Register PostgreSQL Driver
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL Driver not found: " + e.getMessage(), e);
            }
            connection = DriverManager.getConnection(
                    AppConfig.get("db.url", DEFAULT_URL),
                    AppConfig.get("db.user", DEFAULT_USER),
                    AppConfig.get("db.password", ""));
            System.out.println("Connection successful to the database.");
        }
        return connection;
    }

    /**
     * Runs the given work in a single transaction: commit if it succeeds, rollback otherwise.
     * DAO methods called inside the work use the same shared connection, so they join the transaction.
     */
    public static synchronized <T> T inTransaction(TransactionWork<T> work) throws SQLException, BusinessRuleException {
        Connection conn = getConnection();
        conn.setAutoCommit(false); // Start transaction
        try {
            T result = work.execute(conn);
            conn.commit();
            return result;
        } catch (SQLException | BusinessRuleException | RuntimeException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
                // Connection is broken; it will be reopened on next use
            }
        }
    }

    /**
     * Closes the database connection.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed.");
            } catch (SQLException e) {
                System.err.println("Error while closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
