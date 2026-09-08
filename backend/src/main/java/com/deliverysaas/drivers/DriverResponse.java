package com.deliverysaas.drivers;

import java.time.Instant;
import java.util.UUID;
import com.deliverysaas.drivers.domain.Driver;
import com.deliverysaas.drivers.domain.DriverStatus;

public record DriverResponse(
    UUID id,
    UUID userId,
    String name,
    String email,
    String licenseNumber,
    String phone,
    DriverStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
            driver.getId(), driver.getUser().getId(), driver.getUser().getName(), driver.getUser().getEmail(),
            driver.getLicenseNumber(), driver.getPhone(), driver.getStatus(),
            driver.getCreatedAt(), driver.getUpdatedAt());
    }
}
