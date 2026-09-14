package com.deliverysaas.users;

import java.time.Instant;
import java.util.UUID;
import com.deliverysaas.users.domain.*;

public record UserResponse(UUID id, String name, String email, UserRole role, UserStatus status,
        Instant createdAt, Instant updatedAt) {
    public static UserResponse from(User u) { return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(),
        u.getStatus(), u.getCreatedAt(), u.getUpdatedAt()); }
}
