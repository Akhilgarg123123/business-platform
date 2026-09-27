package com.akhilgarg.businessplatform.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="customers",indexes = @Index(name = "idx_customer_phone", columnList = "phoneNumber"))
public class Customer {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private String email;
    @Column(nullable = false)
    private Boolean isRegular = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getIsRegular() {
        return isRegular;
    }

    public void setIsRegular(Boolean isRegular) {
        this.isRegular = isRegular;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }



}
