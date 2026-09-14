package com.deliverysaas.customers;
import java.util.*;import org.springframework.http.*;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.web.bind.annotation.*;
import com.deliverysaas.shared.error.ForbiddenException;import com.deliverysaas.shared.security.AuthPrincipal;import com.deliverysaas.users.domain.UserRole;import jakarta.validation.Valid;
@RestController @RequestMapping("/api/v1/customers") public class CustomerController{
 private final CustomerService service;public CustomerController(CustomerService s){service=s;}
 @PostMapping public ResponseEntity<CustomerResponse> create(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody CreateCustomerRequest r){manager(p);return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p.organizationId(),r));}
 @GetMapping public List<CustomerResponse> all(@AuthenticationPrincipal AuthPrincipal p){manager(p);return service.all(p.organizationId());}
 @GetMapping("/{id}") public CustomerResponse one(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){return service.one(p,id);}
 @PutMapping("/{id}") public CustomerResponse update(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody UpdateCustomerRequest r){return service.update(p,id,r);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){manager(p);service.delete(p.organizationId(),id);return ResponseEntity.noContent().build();}
 private void manager(AuthPrincipal p){if(p.role()!=UserRole.ADMIN&&p.role()!=UserRole.MANAGER)throw new ForbiddenException("Manager role required");}
}
