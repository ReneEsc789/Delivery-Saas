package com.deliverysaas.warehouses;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import com.deliverysaas.warehouses.domain.WarehouseInventory;

public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, UUID> {
    List<WarehouseInventory> findAllByWarehouseId(UUID warehouseId);

    Optional<WarehouseInventory> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from WarehouseInventory i where i.warehouse.id = :warehouseId and i.product.id = :productId")
    Optional<WarehouseInventory> findForUpdate(@Param("warehouseId") UUID warehouseId, @Param("productId") UUID productId);
}
