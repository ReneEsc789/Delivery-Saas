package com.deliverysaas.warehouses;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.warehouses.domain.WarehouseInventory;

public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, UUID> {
    List<WarehouseInventory> findAllByWarehouseId(UUID warehouseId);

    Optional<WarehouseInventory> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);
}
