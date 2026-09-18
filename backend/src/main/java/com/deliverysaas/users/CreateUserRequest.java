package com.deliverysaas.users;

import com.deliverysaas.users.domain.UserRole;
import jakarta.validation.constraints.*;

public record CreateUserRequest(@NotBlank @Size(max=150) String name, @NotBlank @Email @Size(max=255) String email,
        @NotBlank @Size(min=8,max=100) String password, @NotNull UserRole role) {}
