package com.akhilgarg.businessplatform.repository;

import com.akhilgarg.businessplatform.entity.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, Long> {
}