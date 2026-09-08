package com.deliverysaas.warehouses;

import java.time.Instant;
import java.util.UUID;
import com.deliverysaas.warehouses.domain.WarehouseInventory;

public record WarehouseInventoryResponse(
    UUID productId,
    String productName,
    String productSku,
    int quantity,
    Instant updatedAt
) {
    public static WarehouseInventoryResponse from(WarehouseInventory inventory) {
        return new WarehouseInventoryResponse(
            inventory.getProduct().getId(), inventory.getProduct().getName(), inventory.getProduct().getSku(),
            inventory.getQuantity(), inventory.getUpdatedAt());
    }
}
