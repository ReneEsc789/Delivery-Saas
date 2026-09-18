package com.deliverysaas.deliveries;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.deliveries.domain.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    List<Delivery> findAllByOrganizationId(UUID organizationId);

    List<Delivery> findAllByOrganizationIdAndDriverUserId(UUID organizationId, UUID userId);

    List<Delivery> findAllByOrganizationIdAndOrderCustomerUserId(UUID organizationId, UUID userId);

    Optional<Delivery> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<Delivery> findByOrderIdAndOrganizationId(UUID orderId, UUID organizationId);

    boolean existsByOrderId(UUID orderId);
}
