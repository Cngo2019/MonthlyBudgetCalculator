package com.org.budgettracker;

import com.org.budgettracker.models.api.BudgetCalculation;
import com.org.budgettracker.models.enums.ExpenseGroup;
import com.org.budgettracker.models.implementation.BiWeeklyTakeHomePay;
import com.org.budgettracker.models.implementation.BudgetCalculationBiweekly;
import com.org.budgettracker.models.implementation.Expense;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ExpenseCreatorApp extends Application {

    private final ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Expense Entry Form");

        // Input fields
        TextField nameField = new TextField();
        nameField.setPromptText("Expense Name");

        TextField costField = new TextField();
        costField.setPromptText("Cost");

        ComboBox<ExpenseGroup> groupComboBox = new ComboBox<>();
        groupComboBox.getItems().addAll(ExpenseGroup.values());
        groupComboBox.setPromptText("Select Expense Group");

        // List to show added expenses
        ListView<Expense> listView = new ListView<>(expenseList);
        listView.setPrefHeight(200);


        TextField biweeklyInput = new TextField();
        biweeklyInput.setPromptText("Take home pay (biweekly)");

        // Buttons
        Button addButton = new Button("➕ Add Expense");
        Button submitButton = new Button("✅ Submit All");
        Label statusLabel = new Label();

        addButton.setOnAction(e -> {
            String name = nameField.getText();
            String costText = costField.getText();
            ExpenseGroup group = groupComboBox.getValue();

            if (name.isEmpty() || costText.isEmpty() || group == null) {
                statusLabel.setText("⚠️ Please fill out all fields.");
                return;
            }

            try {
                double cost = Double.parseDouble(costText);
                Expense expense = new Expense(name, cost, group);
                expenseList.add(expense);

                // Clear fields for next entry
                nameField.clear();
                costField.clear();
                groupComboBox.getSelectionModel().clearSelection();

                statusLabel.setText("✅ Expense added.");
            } catch (NumberFormatException ex) {
                statusLabel.setText("❌ Invalid cost. Please enter a valid number.");
            }
        });

        submitButton.setOnAction(e -> {

            if (biweeklyInput.getText().isEmpty()) {
                statusLabel.setText("⚠️ Please enter your biweekly take-home amount.");
                return;
            }

            double takeHome;
            try {
                takeHome = Double.parseDouble(biweeklyInput.getText());
            } catch(NumberFormatException ex) {
                statusLabel.setText("Invalid biweekly amount. Please enter a number");
                return;
            }

            if (expenseList.isEmpty()) {
                statusLabel.setText("⚠️ No expenses to submit.");
            } else {
                // Do something with the full list (e.g. save to DB, file, etc.)
                System.out.println("Submitted Expenses:");
                expenseList.forEach(System.out::println);


                BudgetCalculation budget = new BudgetCalculationBiweekly(new BiWeeklyTakeHomePay(takeHome), expenseList);
                ExcelCreator excelCreator = new ExcelCreator(budget);
                excelCreator.generateSpreadSheet();
                statusLabel.setText("🎉 Submitted " + expenseList.size() + " expenses.");
                expenseList.clear();
            }
        });

        VBox form = new VBox(10, nameField, costField, groupComboBox, addButton, listView, biweeklyInput, submitButton, statusLabel);
        form.setPadding(new Insets(20));

        primaryStage.setScene(new Scene(form, 400, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
