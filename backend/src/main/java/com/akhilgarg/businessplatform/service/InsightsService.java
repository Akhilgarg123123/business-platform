package com.akhilgarg.businessplatform.service;

import com.akhilgarg.businessplatform.dto.*;
import com.akhilgarg.businessplatform.entity.Customer;
import com.akhilgarg.businessplatform.exception.ResourceNotFoundException;
import com.akhilgarg.businessplatform.repository.CustomerRepository;
import com.akhilgarg.businessplatform.repository.ExpenseRepository;
import com.akhilgarg.businessplatform.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class InsightsService {

    private final TransactionRepository transactionRepository;
    private final ExpenseRepository expenseRepository;
    private final CustomerRepository customerRepository;

    public InsightsService(TransactionRepository transactionRepository,
                           ExpenseRepository expenseRepository,
                           CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.expenseRepository = expenseRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public OverviewInsightsResponse getOverview(Long userId) {
        BigDecimal totalRevenue = transactionRepository.sumAmountByUserId(userId);
        BigDecimal totalExpenses = expenseRepository.sumAmountByUserId(userId);
        BigDecimal profit = totalRevenue.subtract(totalExpenses);

        List<TopCustomerResponse> topCustomers = transactionRepository.topCustomersByUserId(userId).stream()
                .limit(5)
                .map(row -> new TopCustomerResponse((Long) row[0], (String) row[1], (BigDecimal) row[2]))
                .toList();

        List<CategoryBreakdownResponse> expenseBreakdown = expenseRepository.sumAmountGroupedByCategory(userId).stream()
                .map(row -> new CategoryBreakdownResponse((String) row[0], (BigDecimal) row[1]))
                .toList();

        List<MonthlyAmountResponse> monthlyRevenue = transactionRepository.monthlyRevenueByUserId(userId).stream()
                .map(row -> new MonthlyAmountResponse((String) row[0], (BigDecimal) row[1]))
                .toList();

        List<MonthlyAmountResponse> monthlyExpenses = expenseRepository.monthlyExpensesByUserId(userId).stream()
                .map(row -> new MonthlyAmountResponse((String) row[0], (BigDecimal) row[1]))
                .toList();

        return new OverviewInsightsResponse(totalRevenue, totalExpenses, profit,
                topCustomers, expenseBreakdown, monthlyRevenue, monthlyExpenses);
    }

    @Transactional(readOnly = true)
    public CustomerInsightsResponse getCustomerInsights(Long customerId, Long userId) {
        // Ownership check - this customer must belong to the current user
        Customer customer =customerRepository.findByIdAndUserId(customerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        BigDecimal totalSpend = transactionRepository.sumAmountByCustomerId(customerId); // placeholder, refined below
        long transactionCount = transactionRepository.countByCustomerId(customerId);
        BigDecimal avgOrderValue = transactionRepository.avgAmountByCustomerId(customerId);
        LocalDate firstPurchase = transactionRepository.findFirstPurchaseDate(customerId);
        LocalDate lastPurchase = transactionRepository.findLastPurchaseDate(customerId);

        Long daysSinceLastPurchase = lastPurchase != null
                ? ChronoUnit.DAYS.between(lastPurchase, LocalDate.now())
                : null;

        List<MonthlyAmountResponse> monthlyTrend = transactionRepository.monthlySpendByCustomerId(customerId).stream()
                .map(row -> new MonthlyAmountResponse((String) row[0], (BigDecimal) row[1]))
                .toList();

        boolean isRegular = Boolean.TRUE.equals(customer.getIsRegular()); // mirrors recalculateRegularStatus's rule for display

        return new CustomerInsightsResponse(customerId, totalSpend, transactionCount, avgOrderValue,
                firstPurchase, lastPurchase, daysSinceLastPurchase, monthlyTrend,
                isRegular ? "regular" : "one-time");
    }
}