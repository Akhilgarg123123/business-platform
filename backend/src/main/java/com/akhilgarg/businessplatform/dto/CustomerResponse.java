package com.akhilgarg.businessplatform.dto;

import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private Boolean isRegular;
    private LocalDateTime createdAt;

    public CustomerResponse(Long id, String name, String phoneNumber, String email,
                            Boolean isRegular, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.isRegular = isRegular;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public Boolean getIsRegular() { return isRegular; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}