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
@RequestMapping("/api/v1/warehouses/{warehouseId}/inventory")
public class WarehouseInventoryController {

    private final WarehouseInventoryService inventoryService;

    public WarehouseInventoryController(WarehouseInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<WarehouseInventoryResponse> create(@AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID warehouseId, @Valid @RequestBody CreateWarehouseInventoryRequest request) {
        requireCatalogManager(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(inventoryService.create(warehouseId, principal.organizationId(), request));
    }

    @GetMapping
    public List<WarehouseInventoryResponse> findAll(@AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID warehouseId) {
        return inventoryService.findAll(warehouseId, principal.organizationId());
    }

    @PutMapping("/{productId}")
    public WarehouseInventoryResponse update(@AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID warehouseId, @PathVariable UUID productId,
            @Valid @RequestBody UpdateWarehouseInventoryRequest request) {
        requireCatalogManager(principal);
        return inventoryService.update(warehouseId, productId, principal.organizationId(), request);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID warehouseId, @PathVariable UUID productId) {
        requireCatalogManager(principal);
        inventoryService.delete(warehouseId, productId, principal.organizationId());
        return ResponseEntity.noContent().build();
    }

    private void requireCatalogManager(AuthPrincipal principal) {
        if (principal.role() != UserRole.ADMIN && principal.role() != UserRole.MANAGER) {
            throw new ForbiddenException("You do not have permission to manage warehouse inventory");
        }
    }
}
