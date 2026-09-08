package com.deliverysaas.auth.login;

import java.util.UUID;

import com.deliverysaas.users.domain.UserRole;

public record LoginResponse(
    UUID userId,
    UUID organizationId,
    String email,
    String name,
    UserRole role
) {}
