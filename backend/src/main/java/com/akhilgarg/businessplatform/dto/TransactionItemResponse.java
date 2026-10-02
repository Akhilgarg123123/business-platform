package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;

public class TransactionItemResponse {

    private Long productId;
    private String productName; // null if no product linked
    private Integer quantity;
    private BigDecimal price;

    public TransactionItemResponse(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}