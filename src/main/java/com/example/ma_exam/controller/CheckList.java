package com.example.ma_exam.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * A ListView with a checkbox per item, used to edit many-to-many associations
 * (courses of a filiere, filieres of a course, enrollments of a student).
 */
class CheckList<T> {

    private final ListView<T> listView;
    private final ToIntFunction<T> idOf;
    private final Map<Integer, BooleanProperty> checks = new HashMap<>();

    CheckList(ListView<T> listView, ToIntFunction<T> idOf) {
        this.listView = listView;
        this.idOf = idOf;
        listView.setCellFactory(CheckBoxListCell.forListView(item -> checkFor(idOf.applyAsInt(item))));
    }

    void setItems(List<T> items) {
        listView.getItems().setAll(items);
    }

    void setChecked(Collection<Integer> ids) {
        checks.values().forEach(check -> check.set(false));
        ids.forEach(id -> checkFor(id).set(true));
    }

    List<Integer> getCheckedIds() {
        return listView.getItems().stream()
                .map(idOf::applyAsInt)
                .filter(id -> checkFor(id).get())
                .collect(Collectors.toList());
    }

    void clear() {
        setChecked(List.of());
    }

    private BooleanProperty checkFor(int id) {
        return checks.computeIfAbsent(id, key -> new SimpleBooleanProperty(false));
    }
}
