package com.deliverysaas.users;

import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.deliverysaas.shared.security.AuthPrincipal;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service; 
    public UserController(UserService service){
        this.service=service;
    }
    @PostMapping 
    public ResponseEntity<UserResponse> create(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody CreateUserRequest r){
        p.requireManager();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p,r));
    }
    @GetMapping 
    public List<UserResponse> all(@AuthenticationPrincipal AuthPrincipal p){
        p.requireManager();return service.all(p);
    }
    @GetMapping("/{id}") 
    public UserResponse one(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){
        p.requireManager();
        return service.one(p,id);
    }
    @PutMapping("/{id}") 
    public UserResponse update(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id,@Valid @RequestBody UpdateUserRequest r){
        p.requireManager();
        return service.update(p,id,r);
    }
    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal p,@PathVariable UUID id){
        p.requireManager();
        service.delete(p,id);
        return ResponseEntity.noContent().build();
    }
}
