package com.example.ma_exam.dao;

import com.example.ma_exam.model.DossierAdministratif;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DossierDAO {

    public void add(DossierAdministratif dossier) throws SQLException {
        String sql = "INSERT INTO dossier_administratif (numero_inscription, date_creation, eleve_id) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, dossier.getNumeroInscription());
            pstmt.setDate(2, Date.valueOf(dossier.getDateCreation()));
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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eleveId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new DossierAdministratif(
                        rs.getInt("id"),
                        rs.getString("numero_inscription"),
                        rs.getDate("date_creation").toLocalDate(),
                        rs.getInt("eleve_id")
                    );
                }
            }
        }
        return null;
    }

    public void update(DossierAdministratif dossier) throws SQLException {
        String sql = "UPDATE dossier_administratif SET numero_inscription = ?, date_creation = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dossier.getNumeroInscription());
            pstmt.setDate(2, Date.valueOf(dossier.getDateCreation()));
            pstmt.setInt(3, dossier.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM dossier_administratif WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<DossierAdministratif> getAll() throws SQLException {
        List<DossierAdministratif> dossiers = new ArrayList<>();
        String sql = "SELECT * FROM dossier_administratif";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dossiers.add(new DossierAdministratif(
                    rs.getInt("id"),
                    rs.getString("numero_inscription"),
                    rs.getDate("date_creation").toLocalDate(),
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
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("numero_inscription"),
                    rs.getDate("date_creation").toString(),
                    rs.getString("prenom") + " " + rs.getString("nom"),
                    rs.getString("matricule")
                });
            }
        }
        return data;
    }
}
