package com.example.ma_exam.controller;

import com.example.ma_exam.dao.CoursDAO;
import com.example.ma_exam.model.Cours;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class CoursController {

    @FXML private TextField txtCode, txtIntitule;
    @FXML private TableView<Cours> tableCours;
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colCode, colIntitule;

    private CoursDAO coursDAO = new CoursDAO();
    private ObservableList<Cours> coursList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colIntitule.setCellValueFactory(new PropertyValueFactory<>("intitule"));
        
        loadData();
    }

    private void loadData() {
        try {
            coursList.clear();
            coursList.addAll(coursDAO.getAll());
            tableCours.setItems(coursList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les cours: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Cours c = new Cours(txtCode.getText(), txtIntitule.getText());
            coursDAO.add(c);
            loadData();
            handleClear();
        } catch (SQLException e) {
            showAlert("Erreur", "Ajout impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setCode(txtCode.getText());
                selected.setIntitule(txtIntitule.getText());
                coursDAO.update(selected);
                loadData();
                handleClear();
            } catch (SQLException e) {
                showAlert("Erreur", "Mise à jour impossible: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                coursDAO.delete(selected.getId());
                loadData();
                handleClear();
            } catch (SQLException e) {
                showAlert("Erreur", "Suppression impossible: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleTableSelection() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtCode.setText(selected.getCode());
            txtIntitule.setText(selected.getIntitule());
        }
    }

    @FXML
    private void handleClear() {
        txtCode.clear();
        txtIntitule.clear();
        tableCours.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
