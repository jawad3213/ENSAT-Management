package com.example.ma_exam.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Collects form validation errors. Usage:
 * <pre>
 * Validator v = new Validator().required("Code", code).maxLength("Code", code, 50);
 * if (!v.isValid()) Alerts.warning("Formulaire invalide", v.getMessage());
 * </pre>
 */
public class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");

    private final List<String> errors = new ArrayList<>();

    /**
     * Trims user input; null stays null.
     */
    public static String clean(String value) {
        return value == null ? null : value.trim();
    }

    public Validator required(String label, String value) {
        if (value == null || value.isBlank()) {
            errors.add("Le champ « " + label + " » est obligatoire.");
        }
        return this;
    }

    public Validator required(String label, Object value) {
        if (value == null) {
            errors.add("Le champ « " + label + " » est obligatoire.");
        }
        return this;
    }

    public Validator maxLength(String label, String value, int max) {
        if (value != null && value.length() > max) {
            errors.add("Le champ « " + label + " » ne doit pas dépasser " + max + " caractères.");
        }
        return this;
    }

    public Validator email(String label, String value) {
        if (value != null && !value.isBlank() && !EMAIL.matcher(value).matches()) {
            errors.add("Le champ « " + label + " » doit être une adresse email valide.");
        }
        return this;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    public String getMessage() {
        return String.join("\n", errors);
    }
}
