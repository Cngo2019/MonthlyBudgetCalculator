package com.org.budgettracker;

import com.org.budgettracker.exceptions.ExcelCreationException;
import com.org.budgettracker.models.api.BudgetCalculation;
import com.org.budgettracker.models.enums.ExpenseGroup;
import com.org.budgettracker.models.implementation.Expense;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelCreator {
    private final BudgetCalculation budget;
    private final String excelOutputName;

    public ExcelCreator(BudgetCalculation budget, String excelOutputName) {
        this.budget = budget;
        this.excelOutputName = excelOutputName;
    }

    public void generateSpreadSheet() throws ExcelCreationException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Expenses");

        int rowAfterSummary = createTotalExpensesColumns(sheet);
        createRemainingAmountColumns(sheet);
        createIndividualExpensesSection(sheet, rowAfterSummary + 3);

        writeSheet(workbook);
        close(workbook);
    }

    private void close(Workbook workbook) throws ExcelCreationException {
        try {
            workbook.close();
        } catch (IOException e) {
            throw new ExcelCreationException();
        }
    }

    private void writeSheet(Workbook workbook) throws ExcelCreationException {
        try (FileOutputStream fileOut = new FileOutputStream("data/ " + excelOutputName + ".xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new ExcelCreationException();
        }
    }

    /**
     * Writes total expenses by group and returns the row index after the summary section
     */
    private int createTotalExpensesColumns(Sheet sheet) {
        Map<ExpenseGroup, Double> totalCostByExpenseType = budget.sumAndGroupExpenses();
        double monthlyTakeHome = round(budget.calculateMonthlyTakeHomePay());

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Expense Type");
        headerRow.createCell(1).setCellValue("Total Expense");
        headerRow.createCell(2).setCellValue("% of Budget");

        int rowNum = 1;
        for (Map.Entry<ExpenseGroup, Double> entry : totalCostByExpenseType.entrySet()) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(entry.getKey().name());

            double totalExpense = round(entry.getValue());
            double percent = round((entry.getValue() / monthlyTakeHome) * 100);

            row.createCell(1).setCellValue(totalExpense);
            row.createCell(2).setCellValue(percent);
            rowNum++;
        }

        // Autosize
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);

        return rowNum;
    }

    private void createRemainingAmountColumns(Sheet sheet) {
        Row takeHomeRow = sheet.getRow(0);
        if (takeHomeRow == null) {
            takeHomeRow = sheet.createRow(0);
        }

        Row valueRow = sheet.getRow(1);
        if (valueRow == null) {
            valueRow = sheet.createRow(1);
        }

        takeHomeRow.createCell(4).setCellValue("Monthly Take-home");
        valueRow.createCell(4).setCellValue(round(budget.calculateMonthlyTakeHomePay()));

        takeHomeRow.createCell(5).setCellValue("Remaining Amount");
        valueRow.createCell(5).setCellValue(round(budget.calculateRemaining()));

        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);
    }

    private void createIndividualExpensesSection(Sheet sheet, int startRow) {
        List<Expense> allExpenses = budget.getExpenses();

        Row header = sheet.createRow(startRow);
        header.createCell(0).setCellValue("Expense Name");
        header.createCell(1).setCellValue("Cost");
        header.createCell(2).setCellValue("Group");

        int row = startRow + 1;
        for (Expense expense : allExpenses) {
            Row dataRow = sheet.createRow(row++);
            dataRow.createCell(0).setCellValue(expense.getExpenseName());
            dataRow.createCell(1).setCellValue(round(expense.getCost()));
            dataRow.createCell(2).setCellValue(expense.getGroup().name());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
