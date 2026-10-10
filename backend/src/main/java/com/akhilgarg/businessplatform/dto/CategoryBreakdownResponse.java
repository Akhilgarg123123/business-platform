package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;

public class CategoryBreakdownResponse {
    private String category;
    private BigDecimal amount;

    public CategoryBreakdownResponse(String category, BigDecimal amount) {
        this.category = category;
        this.amount = amount;
    }

    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
}