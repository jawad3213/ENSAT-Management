package com.example.ma_exam.controller;

import com.example.ma_exam.dao.CoursDAO;
import com.example.ma_exam.dao.EleveDAO;
import com.example.ma_exam.dao.FiliereDAO;
import com.example.ma_exam.model.Cours;
import com.example.ma_exam.model.Filiere;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class StatsController {

    @FXML private Label lblTotalStudents, lblTotalCourses, lblTotalFilieres;
    
    @FXML private TableView<Filiere> tableRecentFilieres;
    @FXML private TableColumn<Filiere, String> colFiliereCode, colFiliereNom;
    
    @FXML private TableView<Cours> tableRecentCourses;
    @FXML private TableColumn<Cours, String> colCourseCode, colCourseNom;

    private EleveDAO eleveDAO = new EleveDAO();
    private CoursDAO coursDAO = new CoursDAO();
    private FiliereDAO filiereDAO = new FiliereDAO();

    @FXML
    public void initialize() {
        setupTables();
        refreshStats();
    }

    private void setupTables() {
        colFiliereCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colFiliereNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colCourseNom.setCellValueFactory(new PropertyValueFactory<>("intitule"));
    }

    public void refreshStats() {
        try {
            lblTotalStudents.setText(String.valueOf(eleveDAO.getCount()));
            lblTotalCourses.setText(String.valueOf(coursDAO.getCount()));
            lblTotalFilieres.setText(String.valueOf(filiereDAO.getCount()));
            
            // Load mini tables (top 5 or just all for now)
            List<Filiere> filieres = filiereDAO.getAll();
            tableRecentFilieres.setItems(FXCollections.observableArrayList(
                filieres.size() > 5 ? filieres.subList(0, 5) : filieres
            ));
            
            List<Cours> cours = coursDAO.getAll();
            tableRecentCourses.setItems(FXCollections.observableArrayList(
                cours.size() > 5 ? cours.subList(0, 5) : cours
            ));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
