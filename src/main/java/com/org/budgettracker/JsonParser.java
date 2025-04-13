package com.org.budgettracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.budgettracker.models.implementation.Expense;

import java.io.File;
import java.io.IOException;

public class JsonParser {
    public Expense[] parseExpenses(String jsonPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Expense[] expenses = mapper.readValue(new File(jsonPath), Expense[].class);
        return expenses;
    }
}
