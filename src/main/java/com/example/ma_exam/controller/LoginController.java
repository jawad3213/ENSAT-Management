package com.example.ma_exam.controller;

import com.example.ma_exam.util.Alerts;
import com.example.ma_exam.util.AppConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        // Admin account comes from config.properties (or APP_ADMIN_USER / APP_ADMIN_PASSWORD), never from the code
        String expectedUser = AppConfig.get("app.admin.user", "admin");
        String expectedPassword = AppConfig.get("app.admin.password");
        if (expectedPassword == null) {
            Alerts.error("Configuration manquante", "Aucun mot de passe administrateur n'est configuré.\n"
                    + "Définissez app.admin.password dans config.properties (voir config.properties.example).");
            return;
        }

        if (expectedUser.equals(username) && expectedPassword.equals(password)) {
            navigateToDashboard();
        } else {
            Alerts.error("Erreur de connexion", "Nom d'utilisateur ou mot de passe incorrect.");
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
            Alerts.error("Impossible de charger le tableau de bord", e);
        }
    }
}
