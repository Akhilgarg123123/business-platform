package com.akhilgarg.businessplatform.controller;

import com.akhilgarg.businessplatform.dto.TransactionRequest;
import com.akhilgarg.businessplatform.dto.TransactionResponse;
import com.akhilgarg.businessplatform.security.CurrentUser;
import com.akhilgarg.businessplatform.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUser currentUser;

    public TransactionController(TransactionService transactionService, CurrentUser currentUser) {
        this.transactionService = transactionService;
        this.currentUser = currentUser;
    }

    @GetMapping("/customers/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getByCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getByCustomer(id, currentUser.getId()));
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.create(currentUser.getId(), request));
    }
}