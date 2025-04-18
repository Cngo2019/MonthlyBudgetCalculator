package com.org.budgettracker;

import com.org.budgettracker.exceptions.ExcelCreationException;
import com.org.budgettracker.models.api.BudgetCalculation;
import com.org.budgettracker.models.enums.ExpenseGroup;
import com.org.budgettracker.models.implementation.BiWeeklyTakeHomePay;
import com.org.budgettracker.models.implementation.BudgetCalculationBiweekly;
import com.org.budgettracker.models.implementation.Expense;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class ExpenseCreatorApp extends Application {

    private final Map<ExpenseGroup, VBox> expenseInputsMap = new EnumMap<>(ExpenseGroup.class);
    private final List<Expense> expenseList = new ArrayList<>();
    private TextField biweeklyInput;
    private BudgetCalculationBiweekly loadedBudget;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Multi-Column Expense Entry");

        loadSavedBudget(); // Load saved budget if available

        GridPane grid = setupGrid();

        biweeklyInput = new TextField();
        biweeklyInput.setPromptText("Take home pay (biweekly)");

        TextField excelSheetNameIn = new TextField();
        excelSheetNameIn.setPromptText("Excel sheet name");

        if (loadedBudget != null) {
            biweeklyInput.setText(
                    String.valueOf(round(loadedBudget.getTakeHomePay().getPay()))
            );
        }


        CheckBox saveSettingsCheck = new CheckBox("💾 Save these settings for later");
        Button submitButton = new Button("✅ Submit All");
        Label statusLabel = new Label();

        defineOnSubmit(submitButton, statusLabel, biweeklyInput, excelSheetNameIn, saveSettingsCheck);

        VBox root = new VBox(20, grid, biweeklyInput, submitButton, saveSettingsCheck, statusLabel, excelSheetNameIn);
        root.setPadding(new Insets(20));
        primaryStage.setScene(new Scene(root, 950, 600));
        primaryStage.show();
    }

    private void loadSavedBudget() {
        try {
            File settingsDir = new File("settings");
            if (!settingsDir.exists()) {
                System.out.println("ℹ️ Settings folder does not exist.");
                return;
            }

            File budgetFile = new File(settingsDir, "saved_budget.ser");
            if (!budgetFile.exists()) {
                System.out.println("ℹ️ No saved budget file found.");
                return;
            }

            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(budgetFile))) {
                loadedBudget = (BudgetCalculationBiweekly) in.readObject();
                System.out.println("✅ Loaded saved budget from settings folder.");
            }
        } catch (Exception ex) {
            System.out.println("❌ Error loading saved budget: " + ex.getMessage());
        }
    }

    private GridPane setupGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));

        int col = 0;
        int row = 0;

        for (ExpenseGroup group : ExpenseGroup.values()) {
            VBox column = new VBox(10);
            column.setPadding(new Insets(10));
            column.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10;");
            Label title = new Label(group.name());
            title.setStyle("-fx-font-weight: bold;");

            VBox rowsBox = new VBox(5);
            Button addRowBtn = new Button("➕ Add Row");

            addRowBtn.setOnAction(e -> rowsBox.getChildren().add(createExpenseRow(rowsBox)));

            column.getChildren().addAll(title, rowsBox, addRowBtn);
            expenseInputsMap.put(group, rowsBox);
            grid.add(column, col, row);

            col++;
            if (col == 2) {
                col = 0;
                row++;
            }
        }

        // Prefill if budget loaded
        if (loadedBudget != null) {
            for (Expense expense : loadedBudget.getExpenses()) {
                VBox groupBox = expenseInputsMap.get(expense.getGroup());
                if (groupBox != null) {
                    HBox expenseRow = createExpenseRow(groupBox);
                    ((TextField) expenseRow.getChildren().get(0)).setText(expense.getExpenseName());
                    ((TextField) expenseRow.getChildren().get(1)).setText(String.valueOf(expense.getCost()));
                    groupBox.getChildren().add(expenseRow);
                }
            }
        }

        return grid;
    }

    private void defineOnSubmit(Button submitButton,
                                Label statusLabel,
                                TextField biweeklyInput,
                                TextField excelSheetNameIn,
                                CheckBox saveSettingsCheck) {

        submitButton.setOnAction(e -> {
            expenseList.clear(); // Clear previous entries

            for (Map.Entry<ExpenseGroup, VBox> entry : expenseInputsMap.entrySet()) {
                ExpenseGroup group = entry.getKey();
                VBox rows = entry.getValue();

                for (javafx.scene.Node node : rows.getChildren()) {
                    if (node instanceof HBox uiRow) {
                        TextField nameField = (TextField) uiRow.getChildren().get(0);
                        TextField costField = (TextField) uiRow.getChildren().get(1);

                        String name = nameField.getText().trim();
                        String costText = costField.getText().trim();

                        if (!name.isEmpty() && !costText.isEmpty()) {
                            try {
                                double cost = Double.parseDouble(costText);
                                expenseList.add(new Expense(name, cost, group));
                            } catch (NumberFormatException ex) {
                                statusLabel.setText("❌ Invalid cost in group " + group);
                                return;
                            }
                        }
                    }
                }
            }

            if (biweeklyInput.getText().isEmpty()) {
                statusLabel.setText("⚠️ Please enter your biweekly take-home amount.");
                return;
            }

            double takeHome;
            try {
                takeHome = Double.parseDouble(biweeklyInput.getText());
            } catch (NumberFormatException ex) {
                statusLabel.setText("❌ Invalid biweekly amount.");
                return;
            }

            if (expenseList.isEmpty()) {
                statusLabel.setText("⚠️ No expenses entered.");
                return;
            }

            BudgetCalculationBiweekly budget = new BudgetCalculationBiweekly(
                    new BiWeeklyTakeHomePay(takeHome), expenseList
            );

            if (saveSettingsCheck.isSelected()) {
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("settings/saved_budget.ser"))) {
                    out.writeObject(budget);
                    System.out.println("✅ Budget saved.");
                } catch (Exception ex) {
                    System.out.println("❌ Error saving budget: " + ex.getMessage());
                }
            }

            try {
                ExcelCreator excelCreator = new ExcelCreator(budget, excelSheetNameIn.getText().trim());
                excelCreator.generateSpreadSheet();
                expenseList.clear();
                statusLabel.setText("🎉 Submitted! Your excecl sheet can be found at data/" + excelSheetNameIn.getText().trim());
            } catch (ExcelCreationException ex) {
                statusLabel.setText("There was an error creating your excel file. Please try again or restart the application.");
            }
        });
    }

    private HBox createExpenseRow(VBox parentBox) {
        TextField name = new TextField();
        name.setPromptText("Name");

        TextField cost = new TextField();
        cost.setPromptText("Cost");

        Button delete = new Button("❌");
        delete.setOnAction(e -> parentBox.getChildren().remove(delete.getParent()));

        return new HBox(10, name, cost, delete);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
