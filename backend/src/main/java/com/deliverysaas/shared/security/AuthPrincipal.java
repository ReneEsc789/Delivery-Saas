package com.deliverysaas.shared.security;

import java.util.UUID;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.users.domain.UserRole;

public record AuthPrincipal(UUID userId, UUID organizationId, UserRole role) {

    public void requireManager() {
        if (role != UserRole.ADMIN && role != UserRole.MANAGER) {
            throw new ForbiddenException("Manager role required");
        }
    }
}