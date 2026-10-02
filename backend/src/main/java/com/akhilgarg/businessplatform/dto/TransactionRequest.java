package com.akhilgarg.businessplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class TransactionRequest {

    @NotNull(message = "Customer is required")
    private Long customerId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String description; // optional

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<TransactionItemRequest> items;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<TransactionItemRequest> getItems() { return items; }
    public void setItems(List<TransactionItemRequest> items) { this.items = items; }
}