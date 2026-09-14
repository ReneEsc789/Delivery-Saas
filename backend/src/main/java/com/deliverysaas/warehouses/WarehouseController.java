package com.deliverysaas.warehouses;

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
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateWarehouseRequest request) {
        requireCatalogManager(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.create(principal.organizationId(), request));
    }

    @GetMapping
    public List<WarehouseResponse> findAll(@AuthenticationPrincipal AuthPrincipal principal) {
        return warehouseService.findAll(principal.organizationId());
    }

    @GetMapping("/{id}")
    public WarehouseResponse findById(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        return warehouseService.findById(id, principal.organizationId());
    }

    @PutMapping("/{id}")
    public WarehouseResponse update(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        requireCatalogManager(principal);
        return warehouseService.update(id, principal.organizationId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        requireCatalogManager(principal);
        warehouseService.delete(id, principal.organizationId());
        return ResponseEntity.noContent().build();
    }

    private void requireCatalogManager(AuthPrincipal principal) {
        if (principal.role() != UserRole.ADMIN && principal.role() != UserRole.MANAGER) {
            throw new ForbiddenException("You do not have permission to manage warehouses");
        }
    }
}
