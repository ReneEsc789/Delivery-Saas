package com.deliverysaas.vehicles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.deliverysaas.vehicles.domain.Vehicle;
import com.deliverysaas.vehicles.domain.VehicleStatus;
import com.deliverysaas.vehicles.domain.VehicleType;

public record VehicleResponse(
    UUID id,
    String plate,
    String model,
    VehicleType type,
    BigDecimal capacity,
    VehicleStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
            vehicle.getId(), vehicle.getPlate(), vehicle.getModel(), vehicle.getType(), vehicle.getCapacity(),
            vehicle.getStatus(), vehicle.getCreatedAt(), vehicle.getUpdatedAt());
    }
}
