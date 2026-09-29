package com.akhilgarg.businessplatform.repository;

import com.akhilgarg.businessplatform.entity.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByUserId(Long userId);
    List<Customer> findByUserIdAndPhoneNumber(Long userId, String phoneNumber);
    Optional<Customer> findByIdAndUserId(Long id, Long userId);
}