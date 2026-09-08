package com.deliverysaas.warehouses;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateWarehouseInventoryRequest(@NotNull @Min(0) Integer quantity) {}
