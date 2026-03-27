package com.example.ma_exam.dao;

import com.example.ma_exam.model.Filiere;
import com.example.ma_exam.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FiliereDAO {
    
    public void add(Filiere filiere) throws SQLException {
        String sql = "INSERT INTO filiere (code, nom, description) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        List<Filiere> filieres = new ArrayList<>();
        String sql = "SELECT * FROM filiere";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                filieres.add(new Filiere(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("nom"),
                    rs.getString("description")
                ));
            }
        }
        return filieres;
    }

    public void update(Filiere filiere) throws SQLException {
        String sql = "UPDATE filiere SET code = ?, nom = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filiere.getCode());
            pstmt.setString(2, filiere.getNom());
            pstmt.setString(3, filiere.getDescription());
            pstmt.setInt(4, filiere.getId());
            pstmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM filiere WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public Filiere getById(int id) throws SQLException {
        String sql = "SELECT * FROM filiere WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Filiere(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("nom"),
                        rs.getString("description")
                    );
                }
            }
        }
        return null;
    }
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM filiere";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
