package com.deliverysaas.users;

import com.deliverysaas.users.domain.*;
import jakarta.validation.constraints.*;

public record UpdateUserRequest(@NotBlank @Size(max=150) String name, @NotBlank @Email @Size(max=255) String email,
        @NotNull UserRole role, @NotNull UserStatus status) {}
