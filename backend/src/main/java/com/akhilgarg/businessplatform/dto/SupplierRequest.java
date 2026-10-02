package com.akhilgarg.businessplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SupplierRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String dealerName; // optional

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @Email(message = "Email must be valid")
    private String email; // optional

    private String itemsSupplied; // optional

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDealerName() { return dealerName; }
    public void setDealerName(String dealerName) { this.dealerName = dealerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getItemsSupplied() { return itemsSupplied; }
    public void setItemsSupplied(String itemsSupplied) { this.itemsSupplied = itemsSupplied; }
}