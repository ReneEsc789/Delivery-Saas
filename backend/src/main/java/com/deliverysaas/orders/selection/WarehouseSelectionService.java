package com.deliverysaas.orders.selection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.deliverysaas.orders.OrderItemRequest;
import com.deliverysaas.orders.OrderRepository;
import com.deliverysaas.orders.domain.OrderStatus;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.warehouses.WarehouseInventoryRepository;
import com.deliverysaas.warehouses.WarehouseRepository;
import com.deliverysaas.warehouses.domain.Warehouse;
import com.deliverysaas.warehouses.domain.WarehouseInventory;
import com.deliverysaas.warehouses.domain.WarehouseStatus;

@Service
public class WarehouseSelectionService {

    private static final List<OrderStatus> activeStatuses =
            List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING);

    private final WarehouseRepository warehouses;
    private final WarehouseInventoryRepository inventory;
    private final OrderRepository orders;

    public WarehouseSelectionService(WarehouseRepository warehouses,
                                     WarehouseInventoryRepository inventory,
                                     OrderRepository orders) {
        this.warehouses = warehouses;
        this.inventory = inventory;
        this.orders = orders;
    }

    public Warehouse select(UUID organizationId, List<OrderItemRequest> items,
                            BigDecimal latitude, BigDecimal longitude) {
        Map<UUID, Integer> requested = new HashMap<>();
        for (OrderItemRequest item : items) {
            if (requested.put(item.productId(), item.quantity()) != null) {
                throw new ConflictException("Products cannot be duplicated");
            }
        }

        List<WarehouseCandidate> candidates = new ArrayList<>();
        for (Warehouse warehouse : warehouses.findAllByOrganizationId(organizationId)) {
            if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
                continue;
            }
            Map<UUID, Integer> stock = new HashMap<>();
            for (WarehouseInventory row : inventory.findAllByWarehouseId(warehouse.getId())) {
                stock.put(row.getProduct().getId(), row.getQuantity());
            }
            long activeOrders = orders.countByWarehouseIdAndStatusIn(warehouse.getId(), activeStatuses);
            candidates.add(new WarehouseCandidate(warehouse, stock, activeOrders));
        }

        return WarehouseSelectionAlgorithm.selectBest(
                        candidates, requested, latitude.doubleValue(), longitude.doubleValue())
                .orElseThrow(() -> new ConflictException("No active warehouse has enough stock for this order"));
    }
}
