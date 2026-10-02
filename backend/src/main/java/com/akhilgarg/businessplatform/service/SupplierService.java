package com.akhilgarg.businessplatform.service;

import com.akhilgarg.businessplatform.dto.SupplierRequest;
import com.akhilgarg.businessplatform.dto.SupplierResponse;
import com.akhilgarg.businessplatform.entity.Supplier;
import com.akhilgarg.businessplatform.exception.ResourceNotFoundException;
import com.akhilgarg.businessplatform.repository.SupplierRepository;
import com.akhilgarg.businessplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    public SupplierService(SupplierRepository supplierRepository, UserRepository userRepository) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAll(Long userId) {
        return supplierRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id, Long userId) {
        return toResponse(findOwned(id, userId));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> searchByPhone(Long userId, String phoneNumber) {
        return supplierRepository.findByUserIdAndPhoneNumber(userId, phoneNumber).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SupplierResponse create(Long userId, SupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setUser(userRepository.getReferenceById(userId));
        applyRequest(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Long id, Long userId, SupplierRequest request) {
        Supplier supplier = findOwned(id, userId);
        applyRequest(supplier, request);
        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        supplierRepository.delete(findOwned(id, userId));
    }

    private void applyRequest(Supplier supplier, SupplierRequest request) {
        supplier.setCompanyName(request.getCompanyName());
        supplier.setDealerName(request.getDealerName());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setEmail(request.getEmail());
        supplier.setItemsSupplied(request.getItemsSupplied());
    }

    private Supplier findOwned(Long id, Long userId) {
        return supplierRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(s.getId(), s.getCompanyName(), s.getDealerName(),
                s.getPhoneNumber(), s.getEmail(), s.getItemsSupplied());
    }
}