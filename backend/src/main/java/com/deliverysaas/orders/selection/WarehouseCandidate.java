package com.deliverysaas.orders.selection;

import java.util.Map;
import java.util.UUID;

import com.deliverysaas.warehouses.domain.Warehouse;

public record WarehouseCandidate(Warehouse warehouse, Map<UUID, Integer> stock, long activeOrders) {
}
