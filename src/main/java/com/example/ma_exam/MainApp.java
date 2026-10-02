package com.example.ma_exam;

import com.example.ma_exam.util.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("view/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 600);
        stage.setTitle("Système de Gestion Académique");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        DBConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch();
    }
}