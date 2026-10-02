package com.akhilgarg.businessplatform.controller;

import com.akhilgarg.businessplatform.dto.ProductRequest;
import com.akhilgarg.businessplatform.dto.ProductResponse;
import com.akhilgarg.businessplatform.security.CurrentUser;
import com.akhilgarg.businessplatform.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CurrentUser currentUser;

    public ProductController(ProductService productService, CurrentUser currentUser) {
        this.productService = productService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAll(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id, currentUser.getId()));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.create(currentUser.getId(), request));
    }
}