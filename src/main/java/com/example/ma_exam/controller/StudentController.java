package com.example.ma_exam.controller;

import com.example.ma_exam.dao.EleveDAO;
import com.example.ma_exam.dao.FiliereDAO;
import com.example.ma_exam.model.Eleve;
import com.example.ma_exam.model.Filiere;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.example.ma_exam.dao.DossierDAO;
import com.example.ma_exam.model.DossierAdministratif;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StudentController {

    @FXML private TextField txtMatricule, txtNom, txtPrenom, txtEmail;
    @FXML private ComboBox<Filiere> comboFiliere;
    @FXML private TableView<Eleve> tableStudents;
    @FXML private TableColumn<Eleve, String> colMatricule, colNom, colPrenom, colEmail, colFiliere, colStatus;

    private EleveDAO eleveDAO = new EleveDAO();
    private FiliereDAO filiereDAO = new FiliereDAO();
    private Map<Integer, String> filiereNames = new HashMap<>();

    @FXML
    public void initialize() {
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Custom value factory for filiere name
        colFiliere.setCellValueFactory(cellData -> {
            String name = filiereNames.get(cellData.getValue().getFiliereId());
            return new SimpleStringProperty(name != null ? name : "Inconnue");
        });

        
        loadFilieres();
        loadStudents();
    }

    private void loadFilieres() {
        try {
            ObservableList<Filiere> filieres = FXCollections.observableArrayList(filiereDAO.getAll());
            comboFiliere.setItems(filieres);
            for (Filiere f : filieres) {
                filiereNames.put(f.getId(), f.getNom());
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Chargement filières échoué: " + e.getMessage());
        }
    }

    private void loadStudents() {
        try {
            tableStudents.setItems(FXCollections.observableArrayList(eleveDAO.getAll()));
        } catch (SQLException e) {
            showAlert("Erreur", "Chargement étudiants échoué: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            if (comboFiliere.getValue() == null) {
                showAlert("Attention", "Veuillez choisir une filière");
                return;
            }
            Eleve e = new Eleve(txtMatricule.getText(), txtNom.getText(), txtPrenom.getText(), 
                               txtEmail.getText(), comboFiliere.getValue().getId());
            eleveDAO.add(e);
            
            // Automatically create administrative dossier
            DossierDAO dossierDAO = new DossierDAO();
            DossierAdministratif dossier = new DossierAdministratif(
                "INS-" + e.getId(), 
                LocalDate.now(), 
                e.getId()
            );
            dossierDAO.add(dossier);
            
            loadStudents();
            handleClear();
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText(null);
            success.setContentText("Étudiant ajouté et dossier administratif créé avec succès (N° " + dossier.getNumeroInscription() + ")");
            success.showAndWait();
            
        } catch (SQLException e) {
            showAlert("Erreur", "Ajout impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Eleve selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setMatricule(txtMatricule.getText());
                selected.setNom(txtNom.getText());
                selected.setPrenom(txtPrenom.getText());
                selected.setEmail(txtEmail.getText());
                selected.setFiliereId(comboFiliere.getValue().getId());
                // Status remains unchanged
                
                eleveDAO.update(selected);
                loadStudents();
                handleClear();
            } catch (SQLException e) {
                showAlert("Erreur", "Mise à jour impossible: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Eleve selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                eleveDAO.delete(selected.getId());
                loadStudents();
                handleClear();
            } catch (SQLException e) {
                showAlert("Erreur", "Suppression impossible: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleTableSelection() {
        Eleve selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtMatricule.setText(selected.getMatricule());
            txtNom.setText(selected.getNom());
            txtPrenom.setText(selected.getPrenom());
            txtEmail.setText(selected.getEmail());
            
            // Set combo filiere
            for (Filiere f : comboFiliere.getItems()) {
                if (f.getId() == selected.getFiliereId()) {
                    comboFiliere.setValue(f);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleClear() {
        txtMatricule.clear();
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        comboFiliere.setValue(null);
        tableStudents.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
