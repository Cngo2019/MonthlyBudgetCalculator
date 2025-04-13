package com.org.budgettracker;

import com.org.budgettracker.models.api.BudgetCalculation;
import com.org.budgettracker.models.implementation.BudgetCalculationBiweekly;
import com.org.budgettracker.models.enums.ExpenseGroup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ExcelCreator {
    private final BudgetCalculation budget;

    public ExcelCreator(BudgetCalculation budget) {
        this.budget = budget;
    }

    /**
     * Takes the budget object and creates an excel sheet
     * at the path specified in 'dst' with the title of the spreadsheet.
     */
    public void generateSpreadSheet() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Expenses");
        createTotalExpensesColumns(sheet);
        createRemainingAmountColumns(sheet);
        writeSheet(workbook);
        close(workbook);
    }

    private void close(Workbook workbook) {
        // Close the workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSheet(Workbook workbook) {
        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream("testingfile/output.xslx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createTotalExpensesColumns(Sheet sheet) {
        Map<ExpenseGroup, Double> totalCostByExpenseType = budget.sumAndGroupExpenses();
        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Expense Type");
        headerRow.createCell(1).setCellValue("Total Expense");

        // Populate rows with expense data
        int rowNum = 1;
        for (Map.Entry<ExpenseGroup, Double> entry : totalCostByExpenseType.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey().name()); // Assuming ExpenseType is an Enum
            row.createCell(1).setCellValue(entry.getValue());
        }

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);


    }

    private void createRemainingAmountColumns(Sheet sheet) {
        sheet.getRow(0).createCell(3).setCellValue("Monthly Take-home");
        sheet.getRow(1).createCell(3).setCellValue(budget.calculateMonthlyTakeHomePay());
        sheet.getRow(0).createCell(4).setCellValue("Remaining Expense");
        sheet.getRow(1).createCell(4).setCellValue(budget.calculateRemaining());
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
    }



}
