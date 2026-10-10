package com.akhilgarg.businessplatform.repository;

import com.akhilgarg.businessplatform.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerId(Long customerId);

    long countByCustomerIdAndDateAfter(Long customerId, LocalDate date);
    List<Transaction> findByCustomerIdOrderByDateDesc(Long customerId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.customer.id = :customerId")
    BigDecimal sumAmountByCustomerId(Long customerId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.customer.user.id = :userId")
    BigDecimal sumAmountByUserId(Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.customer.id = :customerId")
    long countByCustomerId(Long customerId);

    @Query("SELECT COALESCE(AVG(t.amount), 0) FROM Transaction t WHERE t.customer.id = :customerId")
    BigDecimal avgAmountByCustomerId(Long customerId);

    @Query("SELECT MIN(t.date) FROM Transaction t WHERE t.customer.id = :customerId")
    LocalDate findFirstPurchaseDate(Long customerId);

    @Query("SELECT MAX(t.date) FROM Transaction t WHERE t.customer.id = :customerId")
    LocalDate findLastPurchaseDate(Long customerId);
    @Query("SELECT FUNCTION('DATE_FORMAT', t.date, '%Y-%m') as month, COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t WHERE t.customer.id = :customerId " +
            "GROUP BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m')")
    List<Object[]> monthlySpendByCustomerId(Long customerId);

    @Query("SELECT c.id, c.name, COALESCE(SUM(t.amount), 0) as total FROM Transaction t JOIN t.customer c " +
            "WHERE c.user.id = :userId GROUP BY c.id, c.name ORDER BY total DESC")
    List<Object[]> topCustomersByUserId(Long userId);

    @Query("SELECT FUNCTION('DATE_FORMAT', t.date, '%Y-%m') as month, COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t JOIN t.customer c WHERE c.user.id = :userId " +
            "GROUP BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m')")
    List<Object[]> monthlyRevenueByUserId(Long userId);}