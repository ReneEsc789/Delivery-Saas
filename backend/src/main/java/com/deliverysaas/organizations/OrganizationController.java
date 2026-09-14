package com.deliverysaas.organizations;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.domain.UserRole;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/organization")
public class OrganizationController {
    private final OrganizationService service;
    public OrganizationController(OrganizationService service) { this.service = service; }

    @GetMapping public OrganizationResponse find(@AuthenticationPrincipal AuthPrincipal p) { return service.find(p.organizationId()); }
    @PutMapping public OrganizationResponse update(@AuthenticationPrincipal AuthPrincipal p,
            @Valid @RequestBody UpdateOrganizationRequest r) { requireAdmin(p); return service.update(p.organizationId(), r); }
    @DeleteMapping public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal p) {
        requireAdmin(p); service.delete(p.organizationId()); return ResponseEntity.noContent().build();
    }
    private void requireAdmin(AuthPrincipal p) { if (p.role() != UserRole.ADMIN) throw new ForbiddenException("Admin role required"); }
}
