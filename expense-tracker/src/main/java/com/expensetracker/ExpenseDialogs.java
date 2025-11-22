package com.expensetracker;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class ExpenseDialogs {

    public static Optional<Expense> showExpenseDialog(Stage owner, Expense existing) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(existing == null ? "Add Expense" : "Edit Expense");

        Map<Integer, String> categories = ExpenseDAO.getCategories();

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(categories.values());
        categoryBox.setEditable(true);  // 🔹 allow typing new category names

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 1200.50");

        TextArea noteArea = new TextArea();
        noteArea.setPrefRowCount(3);
        noteArea.setPromptText("Optional notes...");

        // If editing an existing expense, preload values
        if (existing != null) {
            if (existing.getDate() != null) {
                datePicker.setValue(existing.getDate());
            }
            categoryBox.setValue(existing.getCategoryName());
            amountField.setText(String.valueOf(existing.getAmount()));
            noteArea.setText(existing.getNote());
        } else {
            // New expense: select first category by default (if any)
            if (!categoryBox.getItems().isEmpty()) {
                categoryBox.getSelectionModel().select(0);
            }
        }

        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");

        okBtn.setDefaultButton(true);
        cancelBtn.setCancelButton(true);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(8);

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);

        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);

        grid.add(new Label("Amount:"), 0, 2);
        grid.add(amountField, 1, 2);

        grid.add(new Label("Note:"), 0, 3);
        grid.add(noteArea, 1, 3);

        GridPane btnPane = new GridPane();
        btnPane.setHgap(10);
        btnPane.add(okBtn, 0, 0);
        btnPane.add(cancelBtn, 1, 0);
        grid.add(btnPane, 1, 4);

        Scene scene = new Scene(grid, 420, 260);
        dialog.setScene(scene);

        final Expense[] resultHolder = new Expense[1];

        okBtn.setOnAction(e -> {
            try {
                LocalDate date = datePicker.getValue();
                String catName = categoryBox.getEditor().getText(); // 🔹 get typed text
                String amountStr = amountField.getText();
                String note = noteArea.getText();

                if (date == null) {
                    showError("Please select a date.");
                    return;
                }
                if (catName == null || catName.trim().isEmpty()) {
                    showError("Please enter a category.");
                    return;
                }
                if (amountStr == null || amountStr.isBlank()) {
                    showError("Please enter an amount.");
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException ex) {
                    showError("Amount must be a valid number.");
                    return;
                }

                if (amount <= 0) {
                    showError("Amount must be greater than zero.");
                    return;
                }

                // 🔹 Get or create category id in DB
                int catId = ExpenseDAO.getOrCreateCategoryId(catName);

                if (existing == null) {
                    // New expense
                    resultHolder[0] = new Expense(amount, date, catId, catName.trim(), note);
                } else {
                    // Update existing
                    existing.setAmount(amount);
                    existing.setDate(date);
                    existing.setCategoryId(catId);
                    existing.setCategoryName(catName.trim());
                    existing.setNote(note);
                    resultHolder[0] = existing;
                }
                dialog.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Unexpected error: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> {
            resultHolder[0] = null;
            dialog.close();
        });

        dialog.showAndWait();
        return Optional.ofNullable(resultHolder[0]);
    }

    private static void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText("Invalid input");
        alert.showAndWait();
    }
}
