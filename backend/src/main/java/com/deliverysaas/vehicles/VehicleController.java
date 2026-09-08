package com.deliverysaas.vehicles;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.domain.UserRole;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateVehicleRequest request) {
        requireManager(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(principal.organizationId(), request));
    }

    @GetMapping
    public List<VehicleResponse> findAll(@AuthenticationPrincipal AuthPrincipal principal) {
        requireManager(principal);
        return vehicleService.findAll(principal.organizationId());
    }

    @GetMapping("/{id}")
    public VehicleResponse findById(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        requireManager(principal);
        return vehicleService.findById(id, principal.organizationId());
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        requireManager(principal);
        return vehicleService.update(id, principal.organizationId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        requireManager(principal);
        vehicleService.delete(id, principal.organizationId());
        return ResponseEntity.noContent().build();
    }

    private void requireManager(AuthPrincipal principal) {
        if (principal.role() != UserRole.ADMIN && principal.role() != UserRole.MANAGER) {
            throw new ForbiddenException("You do not have permission to manage vehicles");
        }
    }
}
