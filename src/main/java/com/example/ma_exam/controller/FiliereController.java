package com.example.ma_exam.controller;

import com.example.ma_exam.dao.FiliereDAO;
import com.example.ma_exam.model.Filiere;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class FiliereController {

    @FXML private TextField txtCode, txtNom;
    @FXML private TextArea txtDescription;
    @FXML private TableView<Filiere> tableFiliere;
    @FXML private TableColumn<Filiere, Integer> colId;
    @FXML private TableColumn<Filiere, String> colCode, colNom, colDescription;

    private FiliereDAO filiereDAO = new FiliereDAO();
    private ObservableList<Filiere> filiereList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        loadData();
    }

    private void loadData() {
        try {
            filiereList.clear();
            filiereList.addAll(filiereDAO.getAll());
            tableFiliere.setItems(filiereList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les filières: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Filiere f = new Filiere(txtCode.getText(), txtNom.getText(), txtDescription.getText());
            filiereDAO.add(f);
            loadData();
            handleClear();
        } catch (SQLException e) {
            showAlert("Erreur", "Ajout impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Filiere selected = tableFiliere.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setCode(txtCode.getText());
                selected.setNom(txtNom.getText());
                selected.setDescription(txtDescription.getText());
                filiereDAO.update(selected);
                loadData();
                handleClear();
            } catch (SQLException e) {
                showAlert("Erreur", "Mise à jour impossible: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Filiere selected = tableFiliere.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                filiereDAO.delete(selected.getId());
                loadData();
                handleClear();
            } catch (SQLException e) {
                showAlert("Erreur", "Suppression impossible (vérifiez si des étudiants y sont rattachés): " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleTableSelection() {
        Filiere selected = tableFiliere.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtCode.setText(selected.getCode());
            txtNom.setText(selected.getNom());
            txtDescription.setText(selected.getDescription());
        }
    }

    @FXML
    private void handleClear() {
        txtCode.clear();
        txtNom.clear();
        txtDescription.clear();
        tableFiliere.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
