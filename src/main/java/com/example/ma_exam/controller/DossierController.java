package com.example.ma_exam.controller;

import com.example.ma_exam.dao.DossierDAO;
import com.example.ma_exam.dao.EleveDAO;
import com.example.ma_exam.model.DossierAdministratif;
import com.example.ma_exam.model.Eleve;
import com.example.ma_exam.util.Alerts;
import com.example.ma_exam.util.Async;
import com.example.ma_exam.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class DossierController {

    @FXML private ComboBox<Eleve> comboEleve;
    @FXML private TextField txtNumero;
    @FXML private DatePicker dateCreation;
    @FXML private TableView<DossierAdministratif> tableDossiers;
    @FXML private TableColumn<DossierAdministratif, Integer> colId;
    @FXML private TableColumn<DossierAdministratif, LocalDate> colDate;
    @FXML private TableColumn<DossierAdministratif, String> colNum, colStudent, colMatricule;

    private final DossierDAO dossierDAO = new DossierDAO();
    private final EleveDAO eleveDAO = new EleveDAO();

    private record Data(List<DossierAdministratif> dossiers, List<Eleve> elevesSansDossier) {}

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNum.setCellValueFactory(new PropertyValueFactory<>("numeroInscription"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colStudent.setCellValueFactory(new PropertyValueFactory<>("eleveNomComplet"));
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("eleveMatricule"));

        // Suggest a registration number when a student is chosen
        comboEleve.valueProperty().addListener((obs, old, eleve) -> {
            if (eleve != null && (txtNumero.getText() == null || txtNumero.getText().isBlank())) {
                txtNumero.setText("INS-" + eleve.getId());
            }
        });
        // Fill the form on any selection change (mouse or keyboard)
        tableDossiers.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> showSelection(selected));
        dateCreation.setValue(LocalDate.now());

        loadDossiers();
    }

    private void loadDossiers() {
        Async.supply("Impossible de charger les dossiers",
                () -> new Data(dossierDAO.getAllWithStudentInfo(), eleveDAO.getWithoutDossier()),
                data -> {
                    tableDossiers.setItems(FXCollections.observableArrayList(data.dossiers()));
                    // Only students without a dossier can get a new one (one dossier per student)
                    comboEleve.setItems(FXCollections.observableArrayList(data.elevesSansDossier()));
                    comboEleve.setPromptText(data.elevesSansDossier().isEmpty()
                            ? "Tous les élèves ont un dossier" : "Choisir un élève...");
                });
    }

    private String validate(boolean creating) {
        String numero = Validator.clean(txtNumero.getText());
        Validator v = new Validator()
                .required("N° d'inscription", numero).maxLength("N° d'inscription", numero, 50)
                .required("Date de création", dateCreation.getValue());
        if (creating) {
            v.required("Élève", comboEleve.getValue());
        }
        if (!v.isValid()) {
            Alerts.warning("Formulaire invalide", v.getMessage());
            return null;
        }
        return numero;
    }

    @FXML
    private void handleAdd() {
        if (tableDossiers.getSelectionModel().getSelectedItem() != null) {
            Alerts.warning("Création", "Désélectionnez le dossier (bouton Vider) pour en créer un nouveau.");
            return;
        }
        String numero = validate(true);
        if (numero == null) return;
        DossierAdministratif dossier = new DossierAdministratif(numero, dateCreation.getValue(), comboEleve.getValue().getId());
        Async.run("Création impossible", () -> dossierDAO.add(dossier), () -> {
            loadDossiers();
            handleClear();
        });
    }

    @FXML
    private void handleUpdate() {
        DossierAdministratif selected = tableDossiers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un dossier dans le tableau pour le modifier.");
            return;
        }
        String numero = validate(false);
        if (numero == null) return;
        // Work on a copy so the table is untouched if the update fails
        DossierAdministratif updated = new DossierAdministratif(selected.getId(), numero, dateCreation.getValue(), selected.getEleveId());
        Async.run("Mise à jour impossible", () -> dossierDAO.update(updated), () -> {
            loadDossiers();
            handleClear();
        });
    }

    @FXML
    private void handleDelete() {
        DossierAdministratif selected = tableDossiers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un dossier dans le tableau pour le supprimer.");
            return;
        }
        if (!Alerts.confirm("Confirmer la suppression", "Supprimer le dossier N° " + selected.getNumeroInscription()
                + " de " + selected.getEleveNomComplet() + " ?")) {
            return;
        }
        Async.run("Suppression impossible", () -> dossierDAO.delete(selected.getId()), () -> {
            loadDossiers();
            handleClear();
        });
    }

    private void showSelection(DossierAdministratif selected) {
        // The student of an existing dossier cannot be changed
        comboEleve.setDisable(selected != null);
        if (selected != null) {
            comboEleve.setValue(null);
            txtNumero.setText(selected.getNumeroInscription());
            dateCreation.setValue(selected.getDateCreation());
        }
    }

    @FXML
    private void handleClear() {
        tableDossiers.getSelectionModel().clearSelection();
        comboEleve.setValue(null);
        txtNumero.clear();
        dateCreation.setValue(LocalDate.now());
    }
}
