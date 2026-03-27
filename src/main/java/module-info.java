module com.example.ma_exam {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.ma_exam to javafx.fxml;
    opens com.example.ma_exam.model to javafx.base;
    opens com.example.ma_exam.controller to javafx.fxml;

    exports com.example.ma_exam;
    exports com.example.ma_exam.model;
    exports com.example.ma_exam.controller;
    exports com.example.ma_exam.dao;
    exports com.example.ma_exam.util;
}