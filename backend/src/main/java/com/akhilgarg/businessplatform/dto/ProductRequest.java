package com.akhilgarg.businessplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Starting stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer currentStock;

    private String unit; // optional

    private BigDecimal defaultPrice; // optional

    private String barcode; // optional

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getDefaultPrice() { return defaultPrice; }
    public void setDefaultPrice(BigDecimal defaultPrice) { this.defaultPrice = defaultPrice; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}