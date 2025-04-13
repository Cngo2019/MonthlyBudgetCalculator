package com.org.budgettracker.models.implementation;

import com.org.budgettracker.models.api.BudgetCalculation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@ToString
@Getter
public class BudgetCalculationBiweekly implements BudgetCalculation {
    private BiWeeklyTakeHomePay takeHomePay;
    private List<Expense> expenses;
}