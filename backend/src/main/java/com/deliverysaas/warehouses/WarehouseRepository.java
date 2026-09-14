package com.deliverysaas.warehouses;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.warehouses.domain.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    List<Warehouse> findAllByOrganizationId(UUID organizationId);

    Optional<Warehouse> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
