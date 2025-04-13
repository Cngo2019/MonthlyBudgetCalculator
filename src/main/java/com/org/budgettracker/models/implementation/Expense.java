package com.org.budgettracker.models.implementation;

import com.org.budgettracker.models.enums.ExpenseGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.function.Predicate;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Expense {
    private String expenseName;
    private double cost;
    private ExpenseGroup group;

    public boolean matches(ExpenseGroup group) {
        return this.group == group;
    }
}
