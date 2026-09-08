package com.deliverysaas.auth.me;

import java.util.UUID;

import com.deliverysaas.users.domain.UserRole;

public record MeResponse(
    UUID userId,
    UUID organizationId,
    String email,
    String name,
    UserRole role
) {}