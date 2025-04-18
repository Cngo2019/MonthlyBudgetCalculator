package com.org.budgettracker.models.implementation;

import com.org.budgettracker.models.api.TakeHomePay;
import lombok.Data;

import java.io.Serializable;

@Data
public class BiWeeklyTakeHomePay implements TakeHomePay, Serializable {
    private double pay;

    public BiWeeklyTakeHomePay(double pay) {
        this.pay = pay;
    }
    public double calculateMonthlyAmount() {
        return (pay * 26) / 12.0;
    }
}
