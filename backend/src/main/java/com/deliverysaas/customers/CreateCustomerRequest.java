package com.deliverysaas.customers;
import java.util.UUID;
import jakarta.validation.constraints.*;
public record CreateCustomerRequest(@NotNull UUID userId,@Size(max=30) String phone,@Size(max=250) String address,@Size(max=100) String city,@Size(max=100) String state,@Size(max=20) String postalCode){}
