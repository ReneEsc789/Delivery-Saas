package com.deliverysaas.drivers;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDriverRequest(
    @NotNull UUID userId,
    @NotBlank @Size(max = 50) String licenseNumber,
    @NotBlank @Size(max = 30) String phone
) {}
