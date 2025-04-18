package com.org.budgettracker;

import com.org.budgettracker.exceptions.ExcelCreationException;
import com.org.budgettracker.models.api.BudgetCalculation;
import com.org.budgettracker.models.api.TakeHomePay;
import com.org.budgettracker.models.enums.PayPeriodType;
import com.org.budgettracker.models.enums.ExpenseGroup;

import com.org.budgettracker.models.implementation.BiweeklyBudget;
import com.org.budgettracker.models.implementation.Expense;
import com.org.budgettracker.models.implementation.MonthlyBudget;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.io.*;
import java.util.*;

import static com.org.budgettracker.models.enums.PayPeriodType.*;

public class ExpenseCreatorApp extends Application {

    private final Map<ExpenseGroup, VBox> expenseInputsMap = new EnumMap<>(ExpenseGroup.class);
    private final List<Expense> expenseList = new ArrayList<>();
    private BudgetCalculation loadedBudget;
    private PayPeriodType payPeriodType;
    private final Map<String, PayPeriodType> buttonToCaluclationTypeMap = Map.of(
            "Biweekly", BIWEEKLY,
            "Monthly", MONTHLY,
            "Weekly", WEEKLY
    );

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Multi-Column Expense Entry");

        loadSavedBudget(); // Load saved budget if available

        GridPane grid = setupGrid();
        List<Node> payPeriodTypeUiElements = setupRadioButtonOptions();

        TextField takeHomePayIn = new TextField();
        takeHomePayIn.setPromptText("Take home pay (biweekly)");

        TextField excelSheetNameIn = new TextField();
        excelSheetNameIn.setPromptText("Excel sheet name");

        if (loadedBudget != null) {
            takeHomePayIn.setText(
                    String.valueOf(round(loadedBudget.getTakeHomePay()))
            );
        }

        CheckBox saveSettingsCheck = new CheckBox("💾 Save these settings for later");
        Button submitButton = new Button("✅ Submit All");
        Label statusLabel = new Label();

        defineOnSubmit(submitButton, statusLabel, takeHomePayIn, excelSheetNameIn, saveSettingsCheck);

        VBox root = new VBox(20);

        root.getChildren().add(grid);
        root.getChildren().add(takeHomePayIn);
        root.getChildren().add(excelSheetNameIn);
        root.getChildren().add(saveSettingsCheck);
        root.getChildren().add(submitButton);
        root.getChildren().add(statusLabel);
        root.getChildren().addAll(payPeriodTypeUiElements);

        root.setPadding(new Insets(20));
        primaryStage.setScene(new Scene(root, 950, 600));
        primaryStage.show();
    }

    private List<Node> setupRadioButtonOptions() {
        // Create label
        Label label = new Label("Choose an option:");
        // Create radio buttons
        RadioButton option1 = new RadioButton("Monthly");
        RadioButton option2 = new RadioButton("Weekly");
        RadioButton option3 = new RadioButton("Biweekly");

        // Group the radio buttons
        ToggleGroup group = new ToggleGroup();
        option1.setToggleGroup(group);
        option2.setToggleGroup(group);
        option3.setToggleGroup(group);

        // Optional: Set default selection
        option1.setSelected(true);

        // Handle selection changes
        group.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal != null) {
                RadioButton selected = (RadioButton) newVal;
                payPeriodType = buttonToCaluclationTypeMap.get(selected.getText());
            }
        });

        return Arrays.asList(
                label, option1, option2, option3
        );
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
                loadedBudget = (BiweeklyBudget) in.readObject();
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
                                TextField takeHomePayIn,
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

            if (takeHomePayIn.getText().isEmpty()) {
                statusLabel.setText("⚠️ Please enter your biweekly take-home amount.");
                return;
            }

            if (payPeriodType == null) {
                statusLabel.setText("⚠️ Please enter your pay period amount.");
                return;
            }

            double takeHome;
            try {
                takeHome = Double.parseDouble(takeHomePayIn.getText());
            } catch (NumberFormatException ex) {
                statusLabel.setText("❌ Invalid biweekly amount.");
                return;
            }

            if (expenseList.isEmpty()) {
                statusLabel.setText("⚠️ No expenses entered.");
                return;
            }


            BudgetCalculation budget = determineBudgetCalculation(takeHome, expenseList);

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

    private BudgetCalculation determineBudgetCalculation(double takeHome, List<Expense> expenseList) {
        if (payPeriodType == BIWEEKLY) {
            return new BiweeklyBudget(takeHome, expenseList);
        }

        if (payPeriodType == MONTHLY) {
            return new MonthlyBudget(takeHome, expenseList);
        }

        if (payPeriodType == WEEKLY) {
            return new MonthlyBudget(takeHome, expenseList);
        }

        throw new RuntimeException();
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
