package com.example.ma_exam.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Clear JavaFX alerts shared by all controllers.
 */
public final class Alerts {

    private Alerts() {
    }

    public static void error(String title, String content) {
        show(Alert.AlertType.ERROR, title, content);
    }

    public static void error(String title, Throwable error) {
        if (!(error instanceof BusinessRuleException)) {
            error.printStackTrace();
        }
        error(title, describe(error));
    }

    public static void warning(String title, String content) {
        show(Alert.AlertType.WARNING, title, content);
    }

    public static void info(String title, String content) {
        show(Alert.AlertType.INFORMATION, title, content);
    }

    /**
     * @return true if the user clicked OK
     */
    public static boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Turns an exception into a message the user can understand.
     */
    static String describe(Throwable error) {
        if (error instanceof BusinessRuleException) {
            return error.getMessage();
        }
        if (error instanceof SQLException) {
            SQLException sqlError = (SQLException) error;
            String state = sqlError.getSQLState() != null ? sqlError.getSQLState() : "";
            String hint;
            if (state.equals("23505")) {
                hint = "Cette valeur existe déjà (code, matricule, email et N° d'inscription doivent être uniques).";
            } else if (state.equals("23503")) {
                hint = "Opération impossible : cet élément est utilisé par d'autres données.";
            } else if (state.equals("28P01") || state.equals("28000")) {
                hint = "Authentification PostgreSQL refusée : vérifiez db.user / db.password dans config.properties.";
            } else if (state.startsWith("08")) {
                hint = "Base de données inaccessible : vérifiez que PostgreSQL est démarré et la configuration (config.properties).";
            } else {
                hint = "Erreur de base de données.";
            }
            return hint + "\n\nDétail : " + sqlError.getMessage();
        }
        return error.getMessage() != null ? error.getMessage() : error.toString();
    }

    private static void show(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
