package com.akhilgarg.businessplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class DeliveryRequest {

    private String description; // optional

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<ExpenseItemRequest> items;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ExpenseItemRequest> getItems() { return items; }
    public void setItems(List<ExpenseItemRequest> items) { this.items = items; }
}