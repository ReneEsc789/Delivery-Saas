package com.deliverysaas.drivers;

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
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(@AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateDriverRequest request) {
        requireManager(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.create(principal.organizationId(), request));
    }

    @GetMapping
    public List<DriverResponse> findAll(@AuthenticationPrincipal AuthPrincipal principal) {
        requireManager(principal);
        return driverService.findAll(principal.organizationId());
    }

    @GetMapping("/{id}")
    public DriverResponse findById(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        requireManager(principal);
        return driverService.findById(id, principal.organizationId());
    }

    @PutMapping("/{id}")
    public DriverResponse update(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id,
            @Valid @RequestBody UpdateDriverRequest request) {
        requireManager(principal);
        return driverService.update(id, principal.organizationId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        requireManager(principal);
        driverService.delete(id, principal.organizationId());
        return ResponseEntity.noContent().build();
    }

    private void requireManager(AuthPrincipal principal) {
        if (principal.role() != UserRole.ADMIN && principal.role() != UserRole.MANAGER) {
            throw new ForbiddenException("You do not have permission to manage drivers");
        }
    }
}
