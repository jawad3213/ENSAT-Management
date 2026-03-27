package com.example.ma_exam.dao;

import com.example.ma_exam.model.Cours;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    public void add(Cours cours) throws SQLException {
        String sql = "INSERT INTO cours (code, intitule) VALUES (?, ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, cours.getCode());
            pstmt.setString(2, cours.getIntitule());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cours.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Cours> getAll() throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                coursList.add(new Cours(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("intitule")
                ));
            }
        }
        return coursList;
    }

    /**
     * Complex Query: Get courses available for a specific student 
     * based on their filiere.
     */
    public List<Cours> getAvailableForStudent(int studentId) throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        String sql = "SELECT c.* FROM cours c " +
                     "JOIN filiere_cours fc ON c.id = fc.cours_id " +
                     "JOIN eleve e ON fc.filiere_id = e.filiere_id " +
                     "WHERE e.id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    coursList.add(new Cours(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("intitule")
                    ));
                }
            }
        }
        return coursList;
    }

    public void update(Cours cours) throws SQLException {
        String sql = "UPDATE cours SET code = ?, intitule = ? WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cours.getCode());
            pstmt.setString(2, cours.getIntitule());
            pstmt.setInt(3, cours.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM cours WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cours";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
