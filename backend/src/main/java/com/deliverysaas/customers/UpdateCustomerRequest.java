package com.deliverysaas.customers;
import jakarta.validation.constraints.Size;
public record UpdateCustomerRequest(@Size(max=30) String phone,@Size(max=250) String address,@Size(max=100) String city,@Size(max=100) String state,@Size(max=20) String postalCode){}
