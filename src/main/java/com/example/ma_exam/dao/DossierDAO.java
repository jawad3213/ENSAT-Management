package com.example.ma_exam.dao;

import com.example.ma_exam.model.DossierAdministratif;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DossierDAO {

    public void add(DossierAdministratif dossier) throws SQLException {
        String sql = "INSERT INTO dossier_administratif (numero_inscription, date_creation, eleve_id) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, dossier.getNumeroInscription());
            pstmt.setString(2, dossier.getDateCreation().toString());
            pstmt.setInt(3, dossier.getEleveId());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    dossier.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public DossierAdministratif getByEleveId(int eleveId) throws SQLException {
        String sql = "SELECT * FROM dossier_administratif WHERE eleve_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eleveId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new DossierAdministratif(
                        rs.getInt("id"),
                        rs.getString("numero_inscription"),
                        parseDate(rs.getString("date_creation")),
                        rs.getInt("eleve_id")
                    );
                }
            }
        }
        return null;
    }

    public void update(DossierAdministratif dossier) throws SQLException {
        String sql = "UPDATE dossier_administratif SET numero_inscription = ?, date_creation = ? WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dossier.getNumeroInscription());
            pstmt.setString(2, dossier.getDateCreation().toString());
            pstmt.setInt(3, dossier.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM dossier_administratif WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<DossierAdministratif> getAll() throws SQLException {
        List<DossierAdministratif> dossiers = new ArrayList<>();
        String sql = "SELECT * FROM dossier_administratif";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dossiers.add(new DossierAdministratif(
                    rs.getInt("id"),
                    rs.getString("numero_inscription"),
                    parseDate(rs.getString("date_creation")),
                    rs.getInt("eleve_id")
                ));
            }
        }
        return dossiers;
    }

    public List<String[]> getAllWithStudentInfo() throws SQLException {
        List<String[]> data = new ArrayList<>();
        String sql = "SELECT d.*, e.nom, e.prenom, e.matricule FROM dossier_administratif d " +
                     "JOIN eleve e ON d.eleve_id = e.id";
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("numero_inscription"),
                    parseDate(rs.getString("date_creation")).toString(),
                    rs.getString("prenom") + " " + rs.getString("nom"),
                    rs.getString("matricule")
                });
            }
        }
        return data;
    }

    /**
     * Parses a date string that may be either an ISO date ("2026-03-27")
     * or a millisecond timestamp ("1774566000000") from legacy data.
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null) return LocalDate.now();
        try {
            // Try parsing as ISO date first (e.g., "2026-03-27")
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                // Fallback: parse as millisecond timestamp from legacy data
                long millis = Long.parseLong(dateStr);
                return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception ex) {
                return LocalDate.now();
            }
        }
    }
}
