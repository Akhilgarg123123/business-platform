package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;

public class TopCustomerResponse {
    private Long customerId;
    private String name;
    private BigDecimal totalSpend;

    public TopCustomerResponse(Long customerId, String name, BigDecimal totalSpend) {
        this.customerId = customerId;
        this.name = name;
        this.totalSpend = totalSpend;
    }

    public Long getCustomerId() { return customerId; }
    public String getName() { return name; }
    public BigDecimal getTotalSpend() { return totalSpend; }
}