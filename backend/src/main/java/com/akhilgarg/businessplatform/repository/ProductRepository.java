package com.akhilgarg.businessplatform.repository;

import com.akhilgarg.businessplatform.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);
    Optional<Product> findByBarcode(String barcode);
    Optional<Product> findByIdAndUserId(Long id, Long userId);
}