package com.akhilgarg.businessplatform.service;

import com.akhilgarg.businessplatform.dto.CustomerRequest;
import com.akhilgarg.businessplatform.dto.CustomerResponse;
import com.akhilgarg.businessplatform.entity.Customer;
import com.akhilgarg.businessplatform.exception.ResourceNotFoundException;
import com.akhilgarg.businessplatform.repository.CustomerRepository;
import com.akhilgarg.businessplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import com.akhilgarg.businessplatform.repository.TransactionRepository;
import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository,TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.transactionRepository=transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll(Long userId) {
        return customerRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id, Long userId) {
        return toResponse(findOwned(id, userId));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> searchByPhone(Long userId, String phoneNumber) {
        return customerRepository.findByUserIdAndPhoneNumber(userId, phoneNumber).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse create(Long userId, CustomerRequest request) {
        Customer customer = new Customer();
        customer.setUser(userRepository.getReferenceById(userId));
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, Long userId, CustomerRequest request) {
        Customer customer = findOwned(id, userId);
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void recalculateRegularStatus(Long customerId) {
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);
        long recentCount = transactionRepository.countByCustomerIdAndDateAfter(customerId, sixtyDaysAgo);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        customer.setIsRegular(recentCount >= 3);
        customerRepository.save(customer);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        customerRepository.delete(findOwned(id, userId));
    }

    // Single place where ownership is enforced for fetch-by-id
    private Customer findOwned(Long id, Long userId) {
        return customerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getPhoneNumber(),
                c.getEmail(), c.getIsRegular(), c.getCreatedAt());
    }
}