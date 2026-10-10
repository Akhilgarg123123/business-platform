package com.akhilgarg.businessplatform.dto;

import java.math.BigDecimal;
import java.util.List;

public class OverviewInsightsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal profit;
    private List<TopCustomerResponse> topCustomers;
    private List<CategoryBreakdownResponse> expenseBreakdown;
    private List<MonthlyAmountResponse> monthlyRevenue;
    private List<MonthlyAmountResponse> monthlyExpenses;

    public OverviewInsightsResponse(BigDecimal totalRevenue, BigDecimal totalExpenses, BigDecimal profit,
                                    List<TopCustomerResponse> topCustomers,
                                    List<CategoryBreakdownResponse> expenseBreakdown,
                                    List<MonthlyAmountResponse> monthlyRevenue,
                                    List<MonthlyAmountResponse> monthlyExpenses) {
        this.totalRevenue = totalRevenue;
        this.totalExpenses = totalExpenses;
        this.profit = profit;
        this.topCustomers = topCustomers;
        this.expenseBreakdown = expenseBreakdown;
        this.monthlyRevenue = monthlyRevenue;
        this.monthlyExpenses = monthlyExpenses;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public BigDecimal getProfit() { return profit; }
    public List<TopCustomerResponse> getTopCustomers() { return topCustomers; }
    public List<CategoryBreakdownResponse> getExpenseBreakdown() { return expenseBreakdown; }
    public List<MonthlyAmountResponse> getMonthlyRevenue() { return monthlyRevenue; }
    public List<MonthlyAmountResponse> getMonthlyExpenses() { return monthlyExpenses; }
}