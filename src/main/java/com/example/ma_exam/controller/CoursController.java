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

public class CoursController {

    @FXML private TextField txtCode, txtIntitule;
    @FXML private TableView<Cours> tableCours;
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colCode, colIntitule;
    @FXML private ListView<Filiere> listFilieres;

    private final CoursDAO coursDAO = new CoursDAO();
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private CheckList<Filiere> filiereChecks;

    private record Data(List<Cours> cours, List<Filiere> filieres) {}

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colIntitule.setCellValueFactory(new PropertyValueFactory<>("intitule"));

        filiereChecks = new CheckList<>(listFilieres, Filiere::getId);
        // Fill the form on any selection change (mouse or keyboard)
        tableCours.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> showSelection(selected));

        loadData();
    }

    private void loadData() {
        Async.supply("Impossible de charger les cours",
                () -> new Data(coursDAO.getAll(), filiereDAO.getAll()),
                data -> {
                    tableCours.setItems(FXCollections.observableArrayList(data.cours()));
                    filiereChecks.setItems(data.filieres());
                });
    }

    private Cours readForm(int id) {
        String code = Validator.clean(txtCode.getText());
        String intitule = Validator.clean(txtIntitule.getText());
        Validator v = new Validator()
                .required("Code du cours", code).maxLength("Code du cours", code, 50)
                .required("Intitulé", intitule).maxLength("Intitulé", intitule, 150);
        if (!v.isValid()) {
            Alerts.warning("Formulaire invalide", v.getMessage());
            return null;
        }
        return new Cours(id, code, intitule);
    }

    @FXML
    private void handleAdd() {
        Cours c = readForm(0);
        if (c == null) return;
        Async.run("Ajout impossible", () -> coursDAO.add(c), () -> {
            loadData();
            handleClear();
        });
    }

    @FXML
    private void handleUpdate() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un cours dans le tableau pour le modifier.");
            return;
        }
        // Work on a copy so the table is untouched if the update fails
        Cours updated = readForm(selected.getId());
        if (updated == null) return;
        Async.run("Mise à jour impossible", () -> coursDAO.update(updated), () -> {
            loadData();
            handleClear();
        });
    }

    @FXML
    private void handleDelete() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un cours dans le tableau pour le supprimer.");
            return;
        }
        if (!Alerts.confirm("Confirmer la suppression", "Supprimer le cours « " + selected.getIntitule()
                + " » ?\nIl sera retiré des filières et des inscriptions des élèves.")) {
            return;
        }
        Async.run("Suppression impossible", () -> coursDAO.delete(selected.getId()), () -> {
            loadData();
            handleClear();
        });
    }

    @FXML
    private void handleSaveFilieres() {
        Cours selected = tableCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un cours pour l'affecter à des filières.");
            return;
        }
        List<Integer> filiereIds = filiereChecks.getCheckedIds();
        Async.supply("Affectation impossible", () -> coursDAO.setFilieres(selected.getId(), filiereIds), removed ->
                Alerts.info("Affectation enregistrée", "Le cours « " + selected.getIntitule() + " » est proposé par "
                        + filiereIds.size() + " filière(s)."
                        + (removed > 0 ? "\n" + removed + " inscription(s) d'élèves de filières retirées ont été supprimées." : "")));
    }

    private void showSelection(Cours selected) {
        if (selected == null) {
            filiereChecks.clear();
            return;
        }
        txtCode.setText(selected.getCode());
        txtIntitule.setText(selected.getIntitule());
        Async.supply("Chargement des filières impossible", () -> coursDAO.getFiliereIds(selected.getId()), ids -> {
            if (selected == tableCours.getSelectionModel().getSelectedItem()) {
                filiereChecks.setChecked(ids);
            }
        });
    }

    @FXML
    private void handleClear() {
        txtCode.clear();
        txtIntitule.clear();
        tableCours.getSelectionModel().clearSelection();
        filiereChecks.clear();
    }
}
