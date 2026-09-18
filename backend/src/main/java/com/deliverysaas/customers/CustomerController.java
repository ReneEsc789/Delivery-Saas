package com.deliverysaas.customers;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.deliverysaas.shared.security.AuthPrincipal;
import jakarta.validation.Valid;
@RestController 
@RequestMapping("/api/v1/customers") 
public class CustomerController{
    private final CustomerService service;
    public CustomerController(CustomerService s){
        service=s;
    }
    @PostMapping 
    public ResponseEntity<CustomerResponse> create(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody CreateCustomerRequest r){
        p.requireManager();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p.organizationId(),r));
    }
    @GetMapping 
    public List<CustomerResponse> all(@AuthenticationPrincipal AuthPrincipal p){
        p.requireManager();
        return service.all(p.organizationId());
    }
    @GetMapping("/{id}") 
    public CustomerResponse one(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){
        return service.one(p,id);
    }
    @PutMapping("/{id}") 
    public CustomerResponse update(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody UpdateCustomerRequest r){
        return service.update(p,id,r);
    }
    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){
        p.requireManager();service.delete(p.organizationId(),id);
        return ResponseEntity.noContent().build();
    }
}
