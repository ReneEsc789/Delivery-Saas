package com.deliverysaas.drivers;

import com.deliverysaas.drivers.domain.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDriverRequest(
    @NotBlank @Size(max = 50) String licenseNumber,
    @NotBlank @Size(max = 30) String phone,
    @NotNull DriverStatus status
) {}
