package com.deliverysaas.shared.security;

import java.util.UUID;
import com.deliverysaas.users.domain.UserRole;

public record AuthPrincipal(UUID userId, UUID organizationId, UserRole role) {}