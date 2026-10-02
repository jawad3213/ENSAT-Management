package com.example.ma_exam.dao;

import com.example.ma_exam.model.DossierAdministratif;
import com.example.ma_exam.util.BusinessRuleException;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DossierDAO {

    /**
     * Business rule: one administrative dossier per student
     * (also enforced by the UNIQUE constraint on eleve_id).
     */
    public void add(DossierAdministratif dossier) throws SQLException, BusinessRuleException {
        if (getByEleveId(dossier.getEleveId()) != null) {
            throw new BusinessRuleException("Cet élève possède déjà un dossier administratif.");
        }
        String sql = "INSERT INTO dossier_administratif (numero_inscription, date_creation, eleve_id) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        List<DossierAdministratif> dossiers = query(SELECT_WITH_STUDENT + "WHERE d.eleve_id = ?", eleveId);
        return dossiers.isEmpty() ? null : dossiers.get(0);
    }

    public void update(DossierAdministratif dossier) throws SQLException {
        String sql = "UPDATE dossier_administratif SET numero_inscription = ?, date_creation = ? WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dossier.getNumeroInscription());
            pstmt.setDate(2, Date.valueOf(dossier.getDateCreation()));
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

    // JOIN with eleve to display the student's name and matricule
    private static final String SELECT_WITH_STUDENT =
            "SELECT d.*, e.nom, e.prenom, e.matricule FROM dossier_administratif d " +
            "JOIN eleve e ON d.eleve_id = e.id ";

    public List<DossierAdministratif> getAllWithStudentInfo() throws SQLException {
        return query(SELECT_WITH_STUDENT + "ORDER BY d.id", null);
    }

    private List<DossierAdministratif> query(String sql, Integer param) throws SQLException {
        List<DossierAdministratif> dossiers = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (param != null) {
                pstmt.setInt(1, param);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DossierAdministratif dossier = new DossierAdministratif(
                        rs.getInt("id"),
                        rs.getString("numero_inscription"),
                        rs.getDate("date_creation").toLocalDate(),
                        rs.getInt("eleve_id")
                    );
                    dossier.setEleveNomComplet(rs.getString("prenom") + " " + rs.getString("nom"));
                    dossier.setEleveMatricule(rs.getString("matricule"));
                    dossiers.add(dossier);
                }
            }
        }
        return dossiers;
    }
}
