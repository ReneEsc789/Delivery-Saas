package com.deliverysaas.auth.register;

import java.util.UUID;

import com.deliverysaas.users.domain.UserRole;

public record RegisterResponse(
    UUID userId,
    UUID organizationId,
    String email,
    UserRole role
) {}