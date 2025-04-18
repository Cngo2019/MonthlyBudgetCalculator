package com.org.budgettracker.models.implementation;

import com.org.budgettracker.models.api.BudgetCalculation;
import com.org.budgettracker.models.api.TakeHomePay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class MonthlyBudget implements BudgetCalculation, Serializable {
    private double takeHomePay;
    private List<Expense> expenses;

    @Override
    public double calculateMonthlyTakeHomePay() {
        return takeHomePay;
    }

}
