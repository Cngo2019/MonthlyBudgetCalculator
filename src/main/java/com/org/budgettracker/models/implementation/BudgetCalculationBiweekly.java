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
public class BudgetCalculationBiweekly implements BudgetCalculation, Serializable {
    private BiWeeklyTakeHomePay takeHomePay;
    private List<Expense> expenses;
}