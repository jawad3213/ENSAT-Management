package com.example.ma_exam.dao;

import com.example.ma_exam.model.Eleve;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EleveDAO {

    public void add(Eleve eleve) throws SQLException {
        String sql = "INSERT INTO eleve (matricule, nom, prenom, email, filiere_id, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, eleve.getMatricule());
            pstmt.setString(2, eleve.getNom());
            pstmt.setString(3, eleve.getPrenom());
            pstmt.setString(4, eleve.getEmail());
            pstmt.setInt(5, eleve.getFiliereId());
            pstmt.setString(6, eleve.getStatus().name());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    eleve.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Eleve> getAll() throws SQLException {
        List<Eleve> eleves = new ArrayList<>();
        String sql = "SELECT * FROM eleve";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                eleves.add(mapResultSetToEleve(rs));
            }
        }
        return eleves;
    }

    /**
     * Requirement: Implementation of complex query with JOIN to get student with Filiere name.
     */
    public List<String[]> getAllWithFiliereName() throws SQLException {
        List<String[]> data = new ArrayList<>();
        String sql = "SELECT e.*, f.nom as filiere_nom FROM eleve e " +
                     "LEFT JOIN filiere f ON e.filiere_id = f.id";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("matricule"),
                    rs.getString("prenom") + " " + rs.getString("nom"),
                    rs.getString("email"),
                    rs.getString("filiere_nom"),
                    rs.getString("status")
                });
            }
        }
        return data;
    }

    public void update(Eleve eleve) throws SQLException {
        String sql = "UPDATE eleve SET matricule = ?, nom = ?, prenom = ?, email = ?, filiere_id = ?, status = ? WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, eleve.getMatricule());
            pstmt.setString(2, eleve.getNom());
            pstmt.setString(3, eleve.getPrenom());
            pstmt.setString(4, eleve.getEmail());
            pstmt.setInt(5, eleve.getFiliereId());
            pstmt.setString(6, eleve.getStatus().name());
            pstmt.setInt(7, eleve.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM eleve WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Requirement: Manage transactions for course enrollment.
     */
    public void enrollInCourses(int eleveId, List<Integer> courseIds) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false); // Start transaction

            String sql = "INSERT INTO eleve_cours (eleve_id, cours_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Integer courseId : courseIds) {
                    pstmt.setInt(1, eleveId);
                    pstmt.setInt(2, courseId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit(); // Commit transaction
            System.out.println("Enrollment transaction committed.");
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
                System.err.println("Transaction rolled back due to error: " + e.getMessage());
            }
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    private Eleve mapResultSetToEleve(ResultSet rs) throws SQLException {
        return new Eleve(
            rs.getInt("id"),
            rs.getString("matricule"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("email"),
            rs.getInt("filiere_id"),
            Eleve.Status.valueOf(rs.getString("status"))
        );
    }
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM eleve";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
