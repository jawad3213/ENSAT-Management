package com.example.ma_exam.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Simple hardcoded authentication
        if ("admin".equals(username) && "admin".equals(password)) {
            navigateToDashboard();
        } else {
            showAlert("Erreur de connexion", "Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    private void navigateToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/ma_exam/view/dashboard.fxml"));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 650);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le tableau de bord.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
