package com.org.budgettracker.models.implementation;

import com.org.budgettracker.models.api.BudgetCalculation;
import com.org.budgettracker.models.api.TakeHomePay;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Data
public class WeeklyBudget implements BudgetCalculation, Serializable {
    private double takeHomePay;
    private List<Expense> expenses;
    @Override
    public double calculateMonthlyTakeHomePay() {
        return (takeHomePay * 52) / 12.0;
    }
}
