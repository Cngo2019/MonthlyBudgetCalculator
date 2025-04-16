package com.org.budgettracker;

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

import java.util.*;

public class ExpenseCreatorApp extends Application {

    private final Map<ExpenseGroup, VBox> expenseInputsMap = new EnumMap<>(ExpenseGroup.class);
    private final List<Expense> expenseList = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Multi-Column Expense Entry");

        // Layout for the columns
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

        // Biweekly input & submit
        TextField biweeklyInput = new TextField();
        biweeklyInput.setPromptText("Take home pay (biweekly)");

        TextField excelSheetNameIn = new TextField();
        excelSheetNameIn.setPromptText("Excel sheet name");

        Button submitButton = new Button("✅ Submit All");
        Label statusLabel = new Label();

        submitButton.setOnAction(e -> {
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
            } else {
                BudgetCalculation budget = new BudgetCalculationBiweekly(new BiWeeklyTakeHomePay(takeHome), expenseList);
                ExcelCreator excelCreator = new ExcelCreator(budget, excelSheetNameIn.getText().trim());
                excelCreator.generateSpreadSheet();
                expenseList.clear();
                statusLabel.setText("🎉 Submitted " + expenseList.size() + " expenses.");
            }
        });

        VBox root = new VBox(20, grid, biweeklyInput, submitButton, statusLabel, excelSheetNameIn);
        root.setPadding(new Insets(20));

        primaryStage.setScene(new Scene(root, 950, 600));
        primaryStage.show();
    }

    private HBox createExpenseRow(VBox parentBox) {
        TextField name = new TextField();
        name.setPromptText("Name");

        TextField cost = new TextField();
        cost.setPromptText("Cost");

        Button delete = new Button("❌");
        delete.setOnAction(e -> parentBox.getChildren().remove(delete.getParent()));

        HBox row = new HBox(10, name, cost, delete);
        return row;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
