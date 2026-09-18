package com.deliverysaas.customers;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.customers.domain.Customer;
public interface CustomerRepository extends JpaRepository<Customer,UUID>{
 List<Customer> findAllByUserOrganizationId(UUID organizationId);
 Optional<Customer> findByIdAndUserOrganizationId(UUID id,UUID organizationId);
 Optional<Customer> findByUserIdAndUserOrganizationId(UUID userId,UUID organizationId);
 boolean existsByUserId(UUID userId);
}
