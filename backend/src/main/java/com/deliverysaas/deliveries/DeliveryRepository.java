package com.deliverysaas.deliveries;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.deliveries.domain.DeliveryStatus;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    List<Delivery> findAllByOrganizationId(UUID organizationId);

    List<Delivery> findAllByOrganizationIdAndDriverUserId(UUID organizationId, UUID userId);

    List<Delivery> findAllByOrganizationIdAndOrderCustomerUserId(UUID organizationId, UUID userId);

    List<Delivery> findAllByOrganizationIdAndStatus(UUID organizationId, DeliveryStatus status);

    List<Delivery> findAllByOrganizationIdAndStatusIn(UUID organizationId, Collection<DeliveryStatus> statuses);

    Optional<Delivery> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<Delivery> findByOrderIdAndOrganizationId(UUID orderId, UUID organizationId);

    boolean existsByOrderId(UUID orderId);

    long countByDriverIdAndStatusAndDeliveredAtGreaterThanEqual(UUID driverId, DeliveryStatus status, Instant since);
}
