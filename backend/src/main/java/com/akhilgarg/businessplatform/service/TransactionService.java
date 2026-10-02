package com.akhilgarg.businessplatform.service;

import com.akhilgarg.businessplatform.dto.*;
import com.akhilgarg.businessplatform.entity.*;
import com.akhilgarg.businessplatform.exception.ResourceNotFoundException;
import com.akhilgarg.businessplatform.repository.CustomerRepository;
import com.akhilgarg.businessplatform.repository.ProductRepository;
import com.akhilgarg.businessplatform.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CustomerService customerService;

    public TransactionService(TransactionRepository transactionRepository,
                              CustomerRepository customerRepository,
                              ProductRepository productRepository,
                              ProductService productService,
                              CustomerService customerService) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.customerService = customerService;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getByCustomer(Long customerId, Long userId) {
        // Confirms the customer actually belongs to this user before showing anything
        customerRepository.findByIdAndUserId(customerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return transactionRepository.findByCustomerIdOrderByDateDesc(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        Customer customer = customerRepository.findByIdAndUserId(request.getCustomerId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());

        BigDecimal total = BigDecimal.ZERO;

        for (TransactionItemRequest itemRequest : request.getItems()) {
            TransactionItem item = new TransactionItem();
            item.setTransaction(transaction);
            item.setQuantity(itemRequest.getQuantity());

            if (itemRequest.getProductId() != null) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                item.setProduct(product);

                BigDecimal price = itemRequest.getPrice() != null
                        ? itemRequest.getPrice()
                        : product.getDefaultPrice();
                item.setPrice(price);

                productService.decreaseStock(product.getId(), itemRequest.getQuantity());
            } else {
                // No product linked - price must be supplied directly
                item.setPrice(itemRequest.getPrice());
            }

            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            transaction.getItems().add(item);
        }

        transaction.setAmount(total);
        Transaction saved = transactionRepository.save(transaction);

        customerService.recalculateRegularStatus(customer.getId());

        return toResponse(saved);
    }

    private TransactionResponse toResponse(Transaction t) {
        List<TransactionItemResponse> itemResponses = t.getItems().stream()
                .map(i -> new TransactionItemResponse(
                        i.getProduct() != null ? i.getProduct().getId() : null,
                        i.getProduct() != null ? i.getProduct().getName() : null,
                        i.getQuantity(),
                        i.getPrice()
                ))
                .toList();

        return new TransactionResponse(t.getId(), t.getCustomer().getId(), t.getAmount(),
                t.getDate(), t.getDescription(), itemResponses);
    }
}