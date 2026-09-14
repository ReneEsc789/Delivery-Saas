package com.deliverysaas.warehouses;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 255) String address,
    @Size(max = 100) String city,
    @Size(max = 100) String state,
    @Size(max = 20) String postalCode,
    @NotNull @DecimalMin(value = "-90.000000") @DecimalMax(value = "90.000000") BigDecimal latitude,
    @NotNull @DecimalMin(value = "-180.000000") @DecimalMax(value = "180.000000") BigDecimal longitude
) {}
