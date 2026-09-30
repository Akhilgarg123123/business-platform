package com.akhilgarg.businessplatform.dto;

public class AuthResponse {

    private String token;
    private String businessName;
    private String phoneNumber;

    public AuthResponse(String token, String businessName, String phoneNumber) {
        this.token = token;
        this.businessName = businessName;
        this.phoneNumber = phoneNumber;
    }

    public String getToken() { return token; }
    public String getBusinessName() { return businessName; }
    public String getPhoneNumber() { return phoneNumber; }
}