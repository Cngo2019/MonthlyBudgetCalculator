package com.org.budgettracker.models.enums;

public enum ExpenseGroup {
    Truist_Credit(1, "Truist credit"),
    Discover_Credit(2, "Discover credit"),
    Investments(3, "Investments"),
    Other(4, "Other");

    private final int code;
    private final String description;

    // Constructor
    ExpenseGroup(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter for code
    public int getCode() {
        return code;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Optionally, you can add a method to retrieve an ExpenseType by its code.
    public static ExpenseGroup getByCode(int code) {
        for (ExpenseGroup type : ExpenseGroup.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unexpected code: " + code);
    }

    @Override
    public String toString() {
        return this.description;
    }
}
