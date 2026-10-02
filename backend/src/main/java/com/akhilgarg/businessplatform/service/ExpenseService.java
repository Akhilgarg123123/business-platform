package com.akhilgarg.businessplatform.service;

import com.akhilgarg.businessplatform.dto.*;
import com.akhilgarg.businessplatform.entity.*;
import com.akhilgarg.businessplatform.exception.ResourceNotFoundException;
import com.akhilgarg.businessplatform.repository.ExpenseRepository;
import com.akhilgarg.businessplatform.repository.ProductRepository;
import com.akhilgarg.businessplatform.repository.SupplierRepository;
import com.akhilgarg.businessplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, SupplierRepository supplierRepository,
                          ProductRepository productRepository, ProductService productService,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAll(Long userId) {
        return expenseRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    // Manual expense - rent, salaries, utilities, other. No items.
    @Transactional
    public ExpenseResponse create(Long userId, ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setUser(userRepository.getReferenceById(userId));
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());

        return toResponse(expenseRepository.save(expense));
    }

    // Supplier delivery - auto-categorized, auto-totaled, increases stock
    @Transactional
    public ExpenseResponse recordDelivery(Long supplierId, Long userId, DeliveryRequest request) {
        Supplier supplier = supplierRepository.findByIdAndUserId(supplierId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        Expense expense = new Expense();
        expense.setUser(userRepository.getReferenceById(userId));
        expense.setSupplier(supplier);
        expense.setCategory("supplier_payment");
        expense.setDate(LocalDate.now());
        expense.setDescription(request.getDescription());

        BigDecimal total = BigDecimal.ZERO;

        for (ExpenseItemRequest itemRequest : request.getItems()) {
            ExpenseItem item = new ExpenseItem();
            item.setExpense(expense);
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(itemRequest.getPrice());

            if (itemRequest.getProductId() != null) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                item.setProduct(product);

                productService.increaseStock(product.getId(), itemRequest.getQuantity());
            }

            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            expense.getItems().add(item);
        }

        expense.setAmount(total);
        return toResponse(expenseRepository.save(expense));
    }

    private ExpenseResponse toResponse(Expense e) {
        List<TransactionItemResponse> itemResponses = e.getItems().stream()
                .map(i -> new TransactionItemResponse(
                        i.getProduct() != null ? i.getProduct().getId() : null,
                        i.getProduct() != null ? i.getProduct().getName() : null,
                        i.getQuantity(),
                        i.getPrice()
                ))
                .toList();

        return new ExpenseResponse(e.getId(), e.getCategory(), e.getAmount(), e.getDate(),
                e.getSupplier() != null ? e.getSupplier().getId() : null,
                e.getDescription(), itemResponses);
    }
}
