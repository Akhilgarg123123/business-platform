package com.akhilgarg.businessplatform.repository;

import com.akhilgarg.businessplatform.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByUserId(Long userId);
    List<Supplier> findByUserIdAndPhoneNumber(Long userId, String phoneNumber);
}