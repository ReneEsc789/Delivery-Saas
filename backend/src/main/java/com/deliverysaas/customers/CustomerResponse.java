package com.deliverysaas.customers;
import java.time.Instant; import java.util.UUID; import com.deliverysaas.customers.domain.Customer;
public record CustomerResponse(UUID id,UUID userId,String name,String email,String phone,String address,String city,String state,String postalCode,Instant createdAt,Instant updatedAt){
 public static CustomerResponse from(Customer c){return new CustomerResponse(c.getId(),c.getUser().getId(),c.getUser().getName(),c.getUser().getEmail(),c.getPhone(),c.getAddress(),c.getCity(),c.getState(),c.getPostalCode(),c.getCreatedAt(),c.getUpdatedAt());}
}
