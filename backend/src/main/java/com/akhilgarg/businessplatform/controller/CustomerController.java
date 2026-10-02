package com.akhilgarg.businessplatform.controller;

import com.akhilgarg.businessplatform.dto.CustomerRequest;
import com.akhilgarg.businessplatform.dto.CustomerResponse;
import com.akhilgarg.businessplatform.security.CurrentUser;
import com.akhilgarg.businessplatform.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CurrentUser currentUser;

    public CustomerController(CustomerService customerService, CurrentUser currentUser) {
        this.customerService = customerService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAll(currentUser.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> search(@RequestParam String phone) {
        return ResponseEntity.ok(customerService.searchByPhone(currentUser.getId(), phone));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.create(currentUser.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, currentUser.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}