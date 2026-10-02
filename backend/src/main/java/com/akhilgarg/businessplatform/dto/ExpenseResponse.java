package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseResponse {

    private Long id;
    private String category;
    private BigDecimal amount;
    private LocalDate date;
    private Long supplierId; // null for non-supplier expenses
    private String description;
    private List<TransactionItemResponse> items; // reusing the same shape as transaction items

    public ExpenseResponse(Long id, String category, BigDecimal amount, LocalDate date,
                           Long supplierId, String description, List<TransactionItemResponse> items) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.supplierId = supplierId;
        this.description = description;
        this.items = items;
    }

    public Long getId() { return id; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public Long getSupplierId() { return supplierId; }
    public String getDescription() { return description; }
    public List<TransactionItemResponse> getItems() { return items; }
}