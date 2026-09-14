package com.deliverysaas.warehouses;

import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateWarehouseInventoryRequest(
    @NotNull UUID productId,
    @NotNull @Min(0) Integer quantity
) {}
