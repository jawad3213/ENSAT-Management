package com.example.ma_exam.controller;

import com.example.ma_exam.dao.DossierDAO;
import com.example.ma_exam.model.DossierAdministratif;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class DossierController {

    @FXML private TableView<String[]> tableDossiers;
    @FXML private TableColumn<String[], String> colId, colNum, colDate, colStudent, colMatricule;

    private DossierDAO dossierDAO = new DossierDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colNum.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colStudent.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colMatricule.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

        loadDossiers();
    }

    private void loadDossiers() {
        try {
            List<String[]> data = dossierDAO.getAllWithStudentInfo();
            tableDossiers.setItems(FXCollections.observableArrayList(data));
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les dossiers: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
