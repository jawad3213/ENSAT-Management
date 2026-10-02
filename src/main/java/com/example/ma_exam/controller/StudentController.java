package com.example.ma_exam.controller;

import com.example.ma_exam.dao.CoursDAO;
import com.example.ma_exam.dao.DossierDAO;
import com.example.ma_exam.dao.EleveDAO;
import com.example.ma_exam.dao.FiliereDAO;
import com.example.ma_exam.model.Cours;
import com.example.ma_exam.model.DossierAdministratif;
import com.example.ma_exam.model.Eleve;
import com.example.ma_exam.model.Filiere;
import com.example.ma_exam.util.Alerts;
import com.example.ma_exam.util.Async;
import com.example.ma_exam.util.Validator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Objects;

public class StudentController {

    @FXML private TextField txtMatricule, txtNom, txtPrenom, txtEmail;
    @FXML private ComboBox<Filiere> comboFiliere;
    @FXML private ComboBox<Eleve.Status> comboStatus;
    @FXML private TableView<Eleve> tableStudents;
    @FXML private TableColumn<Eleve, String> colMatricule, colNom, colPrenom, colEmail, colFiliere;
    @FXML private TableColumn<Eleve, Eleve.Status> colStatus;
    @FXML private ListView<Cours> listCours;
    @FXML private Label lblCoursInfo;
    @FXML private Button btnSaveCours;

    private final EleveDAO eleveDAO = new EleveDAO();
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final CoursDAO coursDAO = new CoursDAO();
    private final DossierDAO dossierDAO = new DossierDAO();
    private CheckList<Cours> coursChecks;

    private record Data(List<Eleve> eleves, List<Filiere> filieres) {}
    private record Enrollment(List<Cours> available, List<Integer> enrolled) {}

    @FXML
    public void initialize() {
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiereNom")); // From JOIN with filiere
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        comboStatus.setItems(FXCollections.observableArrayList(Eleve.Status.values()));
        comboStatus.setValue(Eleve.Status.ACTIVE);

        coursChecks = new CheckList<>(listCours, Cours::getId);
        // Fill the form on any selection change (mouse or keyboard)
        tableStudents.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> showSelection(selected));
        showEnrollment(null, null);

        loadData();
    }

    private void loadData() {
        Async.supply("Chargement des étudiants échoué",
                () -> new Data(eleveDAO.getAll(), filiereDAO.getAll()),
                data -> {
                    comboFiliere.setItems(FXCollections.observableArrayList(data.filieres()));
                    tableStudents.setItems(FXCollections.observableArrayList(data.eleves()));
                });
    }

    private Eleve readForm(int id) {
        String matricule = Validator.clean(txtMatricule.getText());
        String nom = Validator.clean(txtNom.getText());
        String prenom = Validator.clean(txtPrenom.getText());
        String email = Validator.clean(txtEmail.getText());
        Validator v = new Validator()
                .required("Matricule", matricule).maxLength("Matricule", matricule, 50)
                .required("Prénom", prenom).maxLength("Prénom", prenom, 100)
                .required("Nom", nom).maxLength("Nom", nom, 100)
                .required("Email", email).maxLength("Email", email, 150).email("Email", email)
                .required("Filière", comboFiliere.getValue())
                .required("Statut", comboStatus.getValue());
        if (!v.isValid()) {
            Alerts.warning("Formulaire invalide", v.getMessage());
            return null;
        }
        return new Eleve(id, matricule, nom, prenom, email, comboFiliere.getValue().getId(), comboStatus.getValue());
    }

    @FXML
    private void handleAdd() {
        Eleve e = readForm(0);
        if (e == null) return;
        // Student + administrative dossier are created atomically
        Async.supply("Ajout impossible", () -> eleveDAO.addWithDossier(e), dossier -> {
            loadData();
            handleClear();
            Alerts.info("Succès", "Étudiant ajouté et dossier administratif créé avec succès (N° "
                    + dossier.getNumeroInscription() + ")");
        });
    }

    @FXML
    private void handleUpdate() {
        Eleve selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un étudiant dans le tableau pour le modifier.");
            return;
        }
        // Work on a copy so the table is untouched if the update fails
        Eleve updated = readForm(selected.getId());
        if (updated == null) return;
        Async.supply("Mise à jour impossible", () -> eleveDAO.update(updated), removed -> {
            loadData();
            handleClear();
            if (removed > 0) {
                Alerts.info("Inscriptions mises à jour", removed
                        + " inscription(s) à des cours non proposés par la nouvelle filière ont été supprimées.");
            }
        });
    }

    @FXML
    private void handleDelete() {
        Eleve selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un étudiant dans le tableau pour le supprimer.");
            return;
        }
        if (!Alerts.confirm("Confirmer la suppression", "Supprimer l'étudiant " + selected
                + " ?\nSon dossier administratif et ses inscriptions seront aussi supprimés.")) {
            return;
        }
        Async.run("Suppression impossible", () -> eleveDAO.delete(selected.getId()), () -> {
            loadData();
            handleClear();
        });
    }

    @FXML
    private void handleSaveCours() {
        Eleve selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un étudiant pour gérer ses inscriptions.");
            return;
        }
        List<Integer> coursIds = coursChecks.getCheckedIds();
        // Transactional: all enrollments are saved, or none
        Async.run("Inscription impossible", () -> eleveDAO.setEnrollments(selected.getId(), coursIds), () ->
                Alerts.info("Inscriptions enregistrées", selected + " est inscrit(e) à " + coursIds.size() + " cours."));
    }

    @FXML
    private void handleShowDossier() {
        Eleve selected = tableStudents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.warning("Aucune sélection", "Sélectionnez un étudiant pour voir son dossier administratif.");
            return;
        }
        Async.supply("Chargement du dossier impossible", () -> dossierDAO.getByEleveId(selected.getId()), dossier -> {
            if (dossier == null) {
                Alerts.info("Dossier administratif", selected + " n'a pas de dossier administratif.\n"
                        + "Vous pouvez le créer depuis le module « Dossiers ».");
            } else {
                Alerts.info("Dossier administratif", describe(dossier));
            }
        });
    }

    private static String describe(DossierAdministratif dossier) {
        return "Élève : " + dossier.getEleveNomComplet() + " (" + dossier.getEleveMatricule() + ")\n"
                + "N° d'inscription : " + dossier.getNumeroInscription() + "\n"
                + "Date de création : " + dossier.getDateCreation() + "\n\n"
                + "Modification possible depuis le module « Dossiers ».";
    }

    private void showSelection(Eleve selected) {
        if (selected == null) {
            showEnrollment(null, null);
            return;
        }
        txtMatricule.setText(selected.getMatricule());
        txtNom.setText(selected.getNom());
        txtPrenom.setText(selected.getPrenom());
        txtEmail.setText(selected.getEmail());
        comboStatus.setValue(selected.getStatus());

        // Set combo filiere (empty if the student has none)
        comboFiliere.setValue(null);
        for (Filiere f : comboFiliere.getItems()) {
            if (Objects.equals(f.getId(), selected.getFiliereId())) {
                comboFiliere.setValue(f);
                break;
            }
        }

        // Courses offered by the student's filiere, with current enrollments checked
        Async.supply("Chargement des inscriptions impossible",
                () -> new Enrollment(coursDAO.getAvailableForStudent(selected.getId()),
                        eleveDAO.getEnrolledCourseIds(selected.getId())),
                enrollment -> {
                    if (selected == tableStudents.getSelectionModel().getSelectedItem()) {
                        showEnrollment(selected, enrollment);
                    }
                });
    }

    private void showEnrollment(Eleve eleve, Enrollment enrollment) {
        if (eleve == null) {
            coursChecks.setItems(List.of());
            coursChecks.clear();
            lblCoursInfo.setText("Sélectionnez un étudiant pour gérer ses inscriptions.");
            listCours.setDisable(true);
            btnSaveCours.setDisable(true);
            return;
        }
        coursChecks.setItems(enrollment.available());
        coursChecks.setChecked(enrollment.enrolled());
        boolean suspended = eleve.getStatus() == Eleve.Status.SUSPENDED;
        if (suspended) {
            lblCoursInfo.setText("Élève suspendu : inscription à un cours impossible.");
        } else if (enrollment.available().isEmpty()) {
            lblCoursInfo.setText("Aucun cours proposé par sa filière.");
        } else {
            lblCoursInfo.setText("Cours proposés par sa filière :");
        }
        listCours.setDisable(suspended);
        btnSaveCours.setDisable(suspended || enrollment.available().isEmpty());
    }

    @FXML
    private void handleClear() {
        txtMatricule.clear();
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        comboFiliere.setValue(null);
        comboStatus.setValue(Eleve.Status.ACTIVE);
        tableStudents.getSelectionModel().clearSelection();
        showEnrollment(null, null);
    }
}
