package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;

public class MonthlyAmountResponse {
    private String month;
    private BigDecimal amount;

    public MonthlyAmountResponse(String month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }

    public String getMonth() { return month; }
    public BigDecimal getAmount() { return amount; }
}