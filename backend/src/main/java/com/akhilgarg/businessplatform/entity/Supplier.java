package com.akhilgarg.businessplatform.entity;

import jakarta.persistence.*;

@Entity
@Table(name="suppliers",indexes = @Index(name = "idx_supplier_phone", columnList = "phoneNumber"))
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String companyName;

    @Column
    private String dealerName; // optional - specific point of contact under the company

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private String email; // optional

    @Column(columnDefinition = "TEXT")
    private String itemsSupplied;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getItemsSupplied() {
        return itemsSupplied;
    }

    public void setItemsSupplied(String itemsSupplied) {
        this.itemsSupplied = itemsSupplied;
    }
}
