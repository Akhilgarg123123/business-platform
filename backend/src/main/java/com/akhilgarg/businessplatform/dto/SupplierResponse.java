package com.akhilgarg.businessplatform.dto;

public class SupplierResponse {

    private Long id;
    private String companyName;
    private String dealerName;
    private String phoneNumber;
    private String email;
    private String itemsSupplied;

    public SupplierResponse(Long id, String companyName, String dealerName, String phoneNumber,
                            String email, String itemsSupplied) {
        this.id = id;
        this.companyName = companyName;
        this.dealerName = dealerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.itemsSupplied = itemsSupplied;
    }

    public Long getId() { return id; }
    public String getCompanyName() { return companyName; }
    public String getDealerName() { return dealerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getItemsSupplied() { return itemsSupplied; }
}