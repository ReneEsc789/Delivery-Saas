package com.deliverysaas.vehicles;

import java.math.BigDecimal;
import com.deliverysaas.vehicles.domain.VehicleStatus;
import com.deliverysaas.vehicles.domain.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(
    @NotBlank @Size(max = 20) String plate,
    @Size(max = 100) String model,
    @NotNull VehicleType type,
    @NotNull @DecimalMin(value = "0.01") BigDecimal capacity,
    @NotNull VehicleStatus status
) {}
