package com.akhilgarg.businessplatform.controller;

import com.akhilgarg.businessplatform.dto.SupplierRequest;
import com.akhilgarg.businessplatform.dto.SupplierResponse;
import com.akhilgarg.businessplatform.security.CurrentUser;
import com.akhilgarg.businessplatform.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final CurrentUser currentUser;

    public SupplierController(SupplierService supplierService, CurrentUser currentUser) {
        this.supplierService = supplierService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAll() {
        return ResponseEntity.ok(supplierService.getAll(currentUser.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> search(@RequestParam String phone) {
        return ResponseEntity.ok(supplierService.searchByPhone(currentUser.getId(), phone));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.create(currentUser.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, currentUser.getId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}