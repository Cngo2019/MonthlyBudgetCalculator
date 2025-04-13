package com.org.budgettracker.models.api;

import com.org.budgettracker.models.implementation.Expense;
import com.org.budgettracker.models.enums.ExpenseGroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface BudgetCalculation {
    default double calculateRemaining() {
        return calculateMonthlyTakeHomePay() - totalExpenses();
    }

    default Map<ExpenseGroup, Double> sumAndGroupExpenses() {
        return getExpenses().stream()
                .collect(Collectors.groupingBy(
                        Expense::getGroup,
                        Collectors.summingDouble(Expense::getCost)
                ));
    }

    default double totalExpenses() {
        return getExpenses().stream()
                .mapToDouble(Expense::getCost)
                .sum();
    }

    default double totalExpensesByGroup(ExpenseGroup group) {
        return getExpenses().stream()
                .filter(it -> it.matches(group))
                .mapToDouble(Expense::getCost)
                .sum();
    }

    default double calculateMonthlyTakeHomePay() {
        return getTakeHomePay().calculateMonthlyAmount();
    }

    List<Expense> getExpenses();

    TakeHomePay getTakeHomePay();
}