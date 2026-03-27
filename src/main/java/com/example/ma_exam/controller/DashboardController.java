package com.example.ma_exam.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnDashboard, btnStudents, btnFilieres, btnCourses, btnDossiers, btnLogout;

    private Button currentActiveButton;

    @FXML
    public void initialize() {
        loadView("dashboard-stats.fxml");
        highlightActiveButton(btnDashboard);
    }

    @FXML
    private void handleSideMenuAction(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        
        if (clickedButton == btnDashboard) {
            loadView("dashboard-stats.fxml");
        } else if (clickedButton == btnStudents) {
            loadView("student-view.fxml");
        } else if (clickedButton == btnFilieres) {
            loadView("filiere-view.fxml");
        } else if (clickedButton == btnCourses) {
            loadView("cours-view.fxml");
        } else if (clickedButton == btnDossiers) {
            loadView("dossier-view.fxml");
        }
        
        highlightActiveButton(clickedButton);
    }
    @FXML
    private void handleLogout() {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/ma_exam/view/login-view.fxml"));
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            Scene scene = new Scene(loginRoot, 500, 600);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void highlightActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
        }
        button.getStyleClass().add("nav-button-active");
        currentActiveButton = button;
    }

    private void loadView(String fxmlFile) {
        try {
            Parent fxml = FXMLLoader.load(getClass().getResource("/com/example/ma_exam/view/" + fxmlFile));
            contentArea.getChildren().removeAll();
            contentArea.getChildren().setAll(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
