package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private Long id;
    private String name;
    private Integer currentStock;
    private String unit;
    private BigDecimal defaultPrice;
    private String barcode;

    public ProductResponse(Long id, String name, Integer currentStock, String unit,
                           BigDecimal defaultPrice, String barcode) {
        this.id = id;
        this.name = name;
        this.currentStock = currentStock;
        this.unit = unit;
        this.defaultPrice = defaultPrice;
        this.barcode = barcode;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getCurrentStock() { return currentStock; }
    public String getUnit() { return unit; }
    public BigDecimal getDefaultPrice() { return defaultPrice; }
    public String getBarcode() { return barcode; }
}