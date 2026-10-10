package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CustomerInsightsResponse {
    private Long customerId;
    private BigDecimal totalSpend;
    private long transactionCount;
    private BigDecimal avgOrderValue;
    private LocalDate firstPurchase;
    private LocalDate lastPurchase;
    private Long daysSinceLastPurchase;
    private List<MonthlyAmountResponse> monthlySpendTrend;
    private String status;

    public CustomerInsightsResponse(Long customerId, BigDecimal totalSpend, long transactionCount,
                                    BigDecimal avgOrderValue, LocalDate firstPurchase, LocalDate lastPurchase,
                                    Long daysSinceLastPurchase, List<MonthlyAmountResponse> monthlySpendTrend,
                                    String status) {
        this.customerId = customerId;
        this.totalSpend = totalSpend;
        this.transactionCount = transactionCount;
        this.avgOrderValue = avgOrderValue;
        this.firstPurchase = firstPurchase;
        this.lastPurchase = lastPurchase;
        this.daysSinceLastPurchase = daysSinceLastPurchase;
        this.monthlySpendTrend = monthlySpendTrend;
        this.status = status;
    }

    public Long getCustomerId() { return customerId; }
    public BigDecimal getTotalSpend() { return totalSpend; }
    public long getTransactionCount() { return transactionCount; }
    public BigDecimal getAvgOrderValue() { return avgOrderValue; }
    public LocalDate getFirstPurchase() { return firstPurchase; }
    public LocalDate getLastPurchase() { return lastPurchase; }
    public Long getDaysSinceLastPurchase() { return daysSinceLastPurchase; }
    public List<MonthlyAmountResponse> getMonthlySpendTrend() { return monthlySpendTrend; }
    public String getStatus() { return status; }
}