package com.org.budgettracker.models.implementation;

import com.org.budgettracker.models.api.BudgetCalculation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@ToString
@Getter
public class BiweeklyBudget implements BudgetCalculation, Serializable {
    private double takeHomePay;
    private List<Expense> expenses;
    @Override
    public double calculateMonthlyTakeHomePay() {
        return (takeHomePay * 26) / 12.0;
    }
}