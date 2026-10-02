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

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.customer.user.id = :userId")
    BigDecimal sumAmountByUserId(Long userId);
}