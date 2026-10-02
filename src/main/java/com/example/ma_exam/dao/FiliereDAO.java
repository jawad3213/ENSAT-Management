package com.example.ma_exam.dao;

import com.example.ma_exam.model.Filiere;
import com.example.ma_exam.util.BusinessRuleException;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FiliereDAO {

    // JOIN with eleve to get the number of students per filiere
    private static final String SELECT_WITH_COUNT =
            "SELECT f.*, COUNT(e.id) AS nb_eleves FROM filiere f " +
            "LEFT JOIN eleve e ON e.filiere_id = f.id " +
            "GROUP BY f.id ";

    public void add(Filiere filiere) throws SQLException {
        String sql = "INSERT INTO filiere (code, nom, description) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, filiere.getCode());
            pstmt.setString(2, filiere.getNom());
            pstmt.setString(3, filiere.getDescription());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    filiere.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Filiere> getAll() throws SQLException {
        return query(SELECT_WITH_COUNT + "ORDER BY f.id", 0);
    }

    /**
     * Most recently created filieres (highest ids first).
     */
    public List<Filiere> getRecent(int limit) throws SQLException {
        return query(SELECT_WITH_COUNT + "ORDER BY f.id DESC LIMIT ?", limit);
    }

    private List<Filiere> query(String sql, int limit) throws SQLException {
        List<Filiere> filieres = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (limit > 0) {
                pstmt.setInt(1, limit);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Filiere filiere = new Filiere(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("nom"),
                        rs.getString("description")
                    );
                    filiere.setNbEleves(rs.getInt("nb_eleves"));
                    filieres.add(filiere);
                }
            }
        }
        return filieres;
    }

    public void update(Filiere filiere) throws SQLException {
        String sql = "UPDATE filiere SET code = ?, nom = ?, description = ? WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filiere.getCode());
            pstmt.setString(2, filiere.getNom());
            pstmt.setString(3, filiere.getDescription());
            pstmt.setInt(4, filiere.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Business rule: a filiere that still has students cannot be deleted
     * (also enforced by the ON DELETE RESTRICT foreign key).
     */
    public void delete(int id) throws SQLException, BusinessRuleException {
        DBConnection.inTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM eleve WHERE filiere_id = ?")) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    int nbEleves = rs.getInt(1);
                    if (nbEleves > 0) {
                        throw new BusinessRuleException("Suppression interdite : cette filière contient "
                                + nbEleves + " élève(s). Réaffectez-les ou supprimez-les d'abord.");
                    }
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM filiere WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            return null;
        });
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM filiere";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Ids of the courses offered by a filiere (filiere_cours).
     */
    public List<Integer> getCoursIds(int filiereId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT cours_id FROM filiere_cours WHERE filiere_id = ? ORDER BY cours_id")) {
            pstmt.setInt(1, filiereId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    /**
     * Replaces the courses offered by a filiere, in one transaction.
     * Students of this filiere are unenrolled from courses it no longer offers
     * (rule: a student can only follow courses offered by their filiere).
     * @return number of enrollments removed
     */
    public int setCours(int filiereId, List<Integer> coursIds) throws SQLException, BusinessRuleException {
        return DBConnection.inTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM filiere_cours WHERE filiere_id = ?")) {
                pstmt.setInt(1, filiereId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO filiere_cours (filiere_id, cours_id) VALUES (?, ?)")) {
                for (Integer coursId : coursIds) {
                    pstmt.setInt(1, filiereId);
                    pstmt.setInt(2, coursId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM eleve_cours ec USING eleve e " +
                    "WHERE ec.eleve_id = e.id AND e.filiere_id = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM filiere_cours fc " +
                    "WHERE fc.filiere_id = e.filiere_id AND fc.cours_id = ec.cours_id)")) {
                pstmt.setInt(1, filiereId);
                return pstmt.executeUpdate();
            }
        });
    }
}
