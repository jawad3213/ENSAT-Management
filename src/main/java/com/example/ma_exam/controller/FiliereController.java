package com.example.ma_exam.controller;

import com.example.ma_exam.dao.CoursDAO;
import com.example.ma_exam.dao.FiliereDAO;
import com.example.ma_exam.model.Cours;
import com.example.ma_exam.model.Filiere;
import com.example.ma_exam.util.Alerts;
import com.example.ma_exam.util.Async;
import com.example.ma_exam.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class FiliereController {

    @FXML private TextField txtCode, txtNom;
    @FXML private TextArea txtDescription;
    @FXML private TableView<Filiere> tableFiliere;
    @FXML private TableColumn<Filiere, Integer> colId, colNbEleves;
    @FXML private TableColumn<Filiere, String> colCode, colNom, colDescription;
    @FXML private ListView<Cours> listCours;

    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private CheckList<Cours> coursChecks;

    private record Data(List<Filiere> filieres, List<Cours> cours) {}

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colNbEleves.setCellValueFactory(new PropertyValueFactory<>("nbEleves"));

        coursChecks = new CheckList<>(listCours, Cours::getId);
        // Fill the form on any selection change (mouse or keyboard)
        tableFiliere.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> showSelection(selected));

        loadData();
    }

    private void loadData() {
        Async.supply("Impossible de charger les filières",
                () -> new Data(filiereDAO.getAll(), coursDAO.getAll()),
                data -> {
                    tableFiliere.setItems(FXCollections.observableArrayList(data.filieres()));
                    coursChecks.setItems(data.cours());
                });
    }

    private Filiere readForm(int id) {
        String code = Validator.clean(txtCode.getText());
        String nom = Validator.clean(txtNom.getText());
        String description = Validator.clean(txtDescription.getText());
        Validator v = new Validator()
                .required("Code filière", code).maxLength("Code filière", code, 50)
                .required("Nom", nom).maxLength("Nom", nom, 100);
        if (!v.isValid()) {
            Alerts.warning("Formulaire invalide", v.getMessage());
            return null;
        }
        return new Filiere(id, code, nom, description == null || description.isEmpty() ? null : description);
    }

    @FXML
    private void handleAdd() {
        Filiere f = readForm(0);
        if (f == null) return;
        Async.run("Ajout impossible", () -> filiereDAO.add(f), () -> {
            loadData();
            handleClear();
        });
    }

    @FXML
    private void handleUpdate() {
        Filiere selected = tableFiliere.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez une filière dans le tableau pour la modifier.");
            return;
        }
        // Work on a copy so the table is untouched if the update fails
        Filiere updated = readForm(selected.getId());
        if (updated == null) return;
        Async.run("Mise à jour impossible", () -> filiereDAO.update(updated), () -> {
            loadData();
            handleClear();
        });
    }

    @FXML
    private void handleDelete() {
        Filiere selected = tableFiliere.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez une filière dans le tableau pour la supprimer.");
            return;
        }
        if (selected.getNbEleves() > 0) {
            Alerts.warning("Suppression interdite", "La filière « " + selected.getNom() + " » contient "
                    + selected.getNbEleves() + " élève(s). Réaffectez-les ou supprimez-les d'abord.");
            return;
        }
        if (!Alerts.confirm("Confirmer la suppression", "Supprimer la filière « " + selected.getNom() + " » ?")) {
            return;
        }
        Async.run("Suppression impossible", () -> filiereDAO.delete(selected.getId()), () -> {
            loadData();
            handleClear();
        });
    }

    @FXML
    private void handleSaveCours() {
        Filiere selected = tableFiliere.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez une filière pour lui associer des cours.");
            return;
        }
        List<Integer> coursIds = coursChecks.getCheckedIds();
        Async.supply("Association impossible", () -> filiereDAO.setCours(selected.getId(), coursIds), removed ->
                Alerts.info("Cours associés", "La filière « " + selected.getNom() + " » propose maintenant "
                        + coursIds.size() + " cours."
                        + (removed > 0 ? "\n" + removed + " inscription(s) à des cours retirés ont été supprimées." : "")));
    }

    private void showSelection(Filiere selected) {
        if (selected == null) {
            coursChecks.clear();
            return;
        }
        txtCode.setText(selected.getCode());
        txtNom.setText(selected.getNom());
        txtDescription.setText(selected.getDescription());
        Async.supply("Chargement des cours impossible", () -> filiereDAO.getCoursIds(selected.getId()), ids -> {
            if (selected == tableFiliere.getSelectionModel().getSelectedItem()) {
                coursChecks.setChecked(ids);
            }
        });
    }

    @FXML
    private void handleClear() {
        txtCode.clear();
        txtNom.clear();
        txtDescription.clear();
        tableFiliere.getSelectionModel().clearSelection();
        coursChecks.clear();
    }
}
