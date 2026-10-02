package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionResponse {

    private Long id;
    private Long customerId;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private List<TransactionItemResponse> items;

    public TransactionResponse(Long id, Long customerId, BigDecimal amount, LocalDate date,
                               String description, List<TransactionItemResponse> items) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.items = items;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public List<TransactionItemResponse> getItems() { return items; }
}