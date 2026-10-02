package com.example.ma_exam.controller;

import com.example.ma_exam.dao.CoursDAO;
import com.example.ma_exam.dao.EleveDAO;
import com.example.ma_exam.dao.FiliereDAO;
import com.example.ma_exam.model.Cours;
import com.example.ma_exam.model.Filiere;
import com.example.ma_exam.util.Async;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class StatsController {

    private static final int RECENT_LIMIT = 5;

    @FXML private Label lblTotalStudents, lblTotalCourses, lblTotalFilieres;

    @FXML private TableView<Filiere> tableRecentFilieres;
    @FXML private TableColumn<Filiere, String> colFiliereCode, colFiliereNom;
    @FXML private TableColumn<Filiere, Integer> colFiliereEleves;

    @FXML private TableView<Cours> tableRecentCourses;
    @FXML private TableColumn<Cours, String> colCourseCode, colCourseNom;

    private final EleveDAO eleveDAO = new EleveDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private final FiliereDAO filiereDAO = new FiliereDAO();

    private record Stats(int students, int courses, int filieres, List<Filiere> recentFilieres, List<Cours> recentCourses) {}

    @FXML
    public void initialize() {
        setupTables();
        refreshStats();
    }

    private void setupTables() {
        colFiliereCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colFiliereNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colFiliereEleves.setCellValueFactory(new PropertyValueFactory<>("nbEleves"));

        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colCourseNom.setCellValueFactory(new PropertyValueFactory<>("intitule"));
    }

    public void refreshStats() {
        Async.supply("Impossible de charger les statistiques",
                () -> new Stats(eleveDAO.getCount(), coursDAO.getCount(), filiereDAO.getCount(),
                        filiereDAO.getRecent(RECENT_LIMIT), coursDAO.getRecent(RECENT_LIMIT)),
                stats -> {
                    lblTotalStudents.setText(String.valueOf(stats.students()));
                    lblTotalCourses.setText(String.valueOf(stats.courses()));
                    lblTotalFilieres.setText(String.valueOf(stats.filieres()));
                    tableRecentFilieres.setItems(FXCollections.observableArrayList(stats.recentFilieres()));
                    tableRecentCourses.setItems(FXCollections.observableArrayList(stats.recentCourses()));
                });
    }
}
