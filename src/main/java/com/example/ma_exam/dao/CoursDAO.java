package com.example.ma_exam.dao;

import com.example.ma_exam.model.Cours;
import com.example.ma_exam.util.BusinessRuleException;
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
        return query("SELECT * FROM cours ORDER BY id", null);
    }

    /**
     * Most recently created courses (highest ids first).
     */
    public List<Cours> getRecent(int limit) throws SQLException {
        return query("SELECT * FROM cours ORDER BY id DESC LIMIT ?", limit);
    }

    /**
     * Complex Query: Get courses available for a specific student
     * based on their filiere.
     */
    public List<Cours> getAvailableForStudent(int studentId) throws SQLException {
        String sql = "SELECT c.* FROM cours c " +
                     "JOIN filiere_cours fc ON c.id = fc.cours_id " +
                     "JOIN eleve e ON fc.filiere_id = e.filiere_id " +
                     "WHERE e.id = ? ORDER BY c.id";
        return query(sql, studentId);
    }

    private List<Cours> query(String sql, Integer param) throws SQLException {
        List<Cours> coursList = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (param != null) {
                pstmt.setInt(1, param);
            }
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

    /**
     * Ids of the filieres offering a course (filiere_cours).
     */
    public List<Integer> getFiliereIds(int coursId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT filiere_id FROM filiere_cours WHERE cours_id = ? ORDER BY filiere_id")) {
            pstmt.setInt(1, coursId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    /**
     * Replaces the filieres offering a course, in one transaction.
     * Students whose filiere no longer offers the course are unenrolled from it
     * (rule: a student can only follow courses offered by their filiere).
     * @return number of enrollments removed
     */
    public int setFilieres(int coursId, List<Integer> filiereIds) throws SQLException, BusinessRuleException {
        return DBConnection.inTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM filiere_cours WHERE cours_id = ?")) {
                pstmt.setInt(1, coursId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO filiere_cours (filiere_id, cours_id) VALUES (?, ?)")) {
                for (Integer filiereId : filiereIds) {
                    pstmt.setInt(1, filiereId);
                    pstmt.setInt(2, coursId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM eleve_cours ec USING eleve e " +
                    "WHERE ec.eleve_id = e.id AND ec.cours_id = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM filiere_cours fc " +
                    "WHERE fc.filiere_id = e.filiere_id AND fc.cours_id = ec.cours_id)")) {
                pstmt.setInt(1, coursId);
                return pstmt.executeUpdate();
            }
        });
    }
}
