package com.deliverysaas.warehouses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.deliverysaas.warehouses.domain.Warehouse;
import com.deliverysaas.warehouses.domain.WarehouseStatus;

public record WarehouseResponse(
    UUID id,
    String name,
    String address,
    String city,
    String state,
    String postalCode,
    BigDecimal latitude,
    BigDecimal longitude,
    WarehouseStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
            warehouse.getId(), warehouse.getName(), warehouse.getAddress(), warehouse.getCity(),
            warehouse.getState(), warehouse.getPostalCode(), warehouse.getLatitude(), warehouse.getLongitude(),
            warehouse.getStatus(), warehouse.getCreatedAt(), warehouse.getUpdatedAt());
    }
}
