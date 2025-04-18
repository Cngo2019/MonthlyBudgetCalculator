package com.org.budgettracker.models.implementation;

import com.org.budgettracker.models.enums.ExpenseGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Expense implements Serializable {
    private String expenseName;
    private double cost;
    private ExpenseGroup group;

    public boolean matches(ExpenseGroup group) {
        return this.group == group;
    }
}
