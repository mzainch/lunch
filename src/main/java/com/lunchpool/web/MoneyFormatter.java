package com.lunchpool.web;

import org.springframework.stereotype.Component;

@Component("money")
public class MoneyFormatter {
    public String format(int amount) {
        return Integer.toString(amount);
    }

    public int toAmount(String amount) {
        if (amount == null || amount.isBlank())
            throw new IllegalArgumentException("Amount is required");
        try {
            int value = Integer.parseInt(amount);
            if (value < 0)
                throw new IllegalArgumentException("Amount cannot be negative");
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Amount must be a whole number", ex);
        }
    }

    public int toAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
        return amount;
    }
}
