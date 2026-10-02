package com.akhilgarg.businessplatform.service;

import com.akhilgarg.businessplatform.dto.ProductRequest;
import com.akhilgarg.businessplatform.dto.ProductResponse;
import com.akhilgarg.businessplatform.entity.Product;
import com.akhilgarg.businessplatform.exception.ResourceNotFoundException;
import com.akhilgarg.businessplatform.repository.ProductRepository;
import com.akhilgarg.businessplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(Long userId) {
        return productRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id, Long userId) {
        return toResponse(findOwned(id, userId));
    }

    @Transactional
    public ProductResponse create(Long userId, ProductRequest request) {
        Product product = new Product();
        product.setUser(userRepository.getReferenceById(userId));
        product.setName(request.getName());
        product.setCurrentStock(request.getCurrentStock());
        product.setUnit(request.getUnit());
        product.setDefaultPrice(request.getDefaultPrice());
        product.setBarcode(request.getBarcode());
        return toResponse(productRepository.save(product));
    }

    // Called by TransactionService when a sale includes this product
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setCurrentStock(product.getCurrentStock() - quantity);
        productRepository.save(product);
    }

    // Called by ExpenseService when a delivery includes this product
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setCurrentStock(product.getCurrentStock() + quantity);
        productRepository.save(product);
    }

    private Product findOwned(Long id, Long userId) {
        return productRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getCurrentStock(),
                p.getUnit(), p.getDefaultPrice(), p.getBarcode());
    }
}