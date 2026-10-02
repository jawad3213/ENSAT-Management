package com.example.ma_exam.dao;

import com.example.ma_exam.model.DossierAdministratif;
import com.example.ma_exam.model.Eleve;
import com.example.ma_exam.util.BusinessRuleException;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EleveDAO {

    // JOIN with filiere to get the filiere name of each student
    private static final String SELECT_WITH_FILIERE =
            "SELECT e.*, f.nom AS filiere_nom FROM eleve e " +
            "LEFT JOIN filiere f ON e.filiere_id = f.id ";

    public void add(Eleve eleve) throws SQLException {
        String sql = "INSERT INTO eleve (matricule, nom, prenom, email, filiere_id, status) VALUES (?, ?, ?, ?, ?, CAST(? AS student_status))";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, eleve.getMatricule());
            pstmt.setString(2, eleve.getNom());
            pstmt.setString(3, eleve.getPrenom());
            pstmt.setString(4, eleve.getEmail());
            setFiliereId(pstmt, 5, eleve.getFiliereId());
            pstmt.setString(6, eleve.getStatus().name());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    eleve.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Inserts the student and its administrative dossier in a single transaction:
     * either both are saved or neither is.
     * @return the created dossier
     */
    public DossierAdministratif addWithDossier(Eleve eleve) throws SQLException, BusinessRuleException {
        try {
            return DBConnection.inTransaction(conn -> {
                add(eleve);
                DossierAdministratif dossier = new DossierAdministratif("INS-" + eleve.getId(), LocalDate.now(), eleve.getId());
                new DossierDAO().add(dossier); // Same shared connection, so same transaction
                return dossier;
            });
        } catch (SQLException | BusinessRuleException e) {
            eleve.setId(0); // Rolled back: the generated id no longer exists
            throw e;
        }
    }

    public List<Eleve> getAll() throws SQLException {
        return query(SELECT_WITH_FILIERE + "ORDER BY e.id");
    }

    /**
     * Students that do not have an administrative dossier yet.
     */
    public List<Eleve> getWithoutDossier() throws SQLException {
        return query(SELECT_WITH_FILIERE +
                "WHERE NOT EXISTS (SELECT 1 FROM dossier_administratif d WHERE d.eleve_id = e.id) ORDER BY e.id");
    }

    private List<Eleve> query(String sql) throws SQLException {
        List<Eleve> eleves = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Eleve eleve = mapResultSetToEleve(rs);
                eleve.setFiliereNom(rs.getString("filiere_nom"));
                eleves.add(eleve);
            }
        }
        return eleves;
    }

    /**
     * Updates the student. If their filiere changed, enrollments in courses the new filiere
     * does not offer are removed in the same transaction.
     * @return number of enrollments removed
     */
    public int update(Eleve eleve) throws SQLException, BusinessRuleException {
        return DBConnection.inTransaction(conn -> {
            String sql = "UPDATE eleve SET matricule = ?, nom = ?, prenom = ?, email = ?, filiere_id = ?, status = CAST(? AS student_status) WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, eleve.getMatricule());
                pstmt.setString(2, eleve.getNom());
                pstmt.setString(3, eleve.getPrenom());
                pstmt.setString(4, eleve.getEmail());
                setFiliereId(pstmt, 5, eleve.getFiliereId());
                pstmt.setString(6, eleve.getStatus().name());
                pstmt.setInt(7, eleve.getId());
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM eleve_cours ec WHERE ec.eleve_id = ? " +
                    "AND NOT EXISTS (SELECT 1 FROM filiere_cours fc JOIN eleve e ON e.filiere_id = fc.filiere_id " +
                    "WHERE e.id = ec.eleve_id AND fc.cours_id = ec.cours_id)")) {
                pstmt.setInt(1, eleve.getId());
                return pstmt.executeUpdate();
            }
        });
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
     * Ids of the courses a student is enrolled in (eleve_cours).
     */
    public List<Integer> getEnrolledCourseIds(int eleveId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT cours_id FROM eleve_cours WHERE eleve_id = ? ORDER BY cours_id")) {
            pstmt.setInt(1, eleveId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    /**
     * Requirement: Manage transactions for course enrollment.
     * Replaces the student's enrollments with the given courses, in one transaction. Rules:
     * - a suspended student cannot be enrolled in a (new) course;
     * - a student can only follow courses offered by their filiere.
     */
    public void setEnrollments(int eleveId, List<Integer> courseIds) throws SQLException, BusinessRuleException {
        DBConnection.inTransaction(conn -> {
            Eleve.Status status;
            Integer filiereId;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT status, filiere_id FROM eleve WHERE id = ? FOR UPDATE")) {
                pstmt.setInt(1, eleveId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new BusinessRuleException("Cet élève n'existe plus.");
                    }
                    status = parseStatus(rs.getString("status"));
                    filiereId = rs.getObject("filiere_id", Integer.class);
                }
            }

            Set<Integer> added = new HashSet<>(courseIds);
            added.removeAll(getEnrolledCourseIds(eleveId));
            if (status == Eleve.Status.SUSPENDED && !added.isEmpty()) {
                throw new BusinessRuleException("Un élève suspendu ne peut pas être inscrit à un cours.");
            }

            Set<Integer> offered = new HashSet<>();
            if (filiereId != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT cours_id FROM filiere_cours WHERE filiere_id = ?")) {
                    pstmt.setInt(1, filiereId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            offered.add(rs.getInt(1));
                        }
                    }
                }
            }
            if (!offered.containsAll(courseIds)) {
                throw new BusinessRuleException("Un élève ne peut suivre que des cours proposés par sa filière.");
            }

            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM eleve_cours WHERE eleve_id = ?")) {
                pstmt.setInt(1, eleveId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO eleve_cours (eleve_id, cours_id) VALUES (?, ?)")) {
                for (Integer courseId : courseIds) {
                    pstmt.setInt(1, eleveId);
                    pstmt.setInt(2, courseId);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            return null;
        });
    }

    private Eleve mapResultSetToEleve(ResultSet rs) throws SQLException {
        return new Eleve(
            rs.getInt("id"),
            rs.getString("matricule"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("email"),
            rs.getObject("filiere_id", Integer.class), // null stays null (not 0)
            parseStatus(rs.getString("status"))
        );
    }

    private static Eleve.Status parseStatus(String status) {
        return status != null ? Eleve.Status.valueOf(status) : Eleve.Status.ACTIVE;
    }

    private void setFiliereId(PreparedStatement pstmt, int index, Integer filiereId) throws SQLException {
        if (filiereId != null) {
            pstmt.setInt(index, filiereId);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
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
