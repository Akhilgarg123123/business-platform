package com.akhilgarg.businessplatform.repository;

import com.akhilgarg.businessplatform.entity.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {
}