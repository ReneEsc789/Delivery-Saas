package com.deliverysaas.deliveries;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.deliverysaas.deliveries.domain.Delivery;
public interface DeliveryRepository extends JpaRepository<Delivery,UUID>{List<Delivery> findAllByOrganizationId(UUID organizationId);List<Delivery> findAllByOrganizationIdAndDriverUserId(UUID organizationId,UUID userId);Optional<Delivery> findByIdAndOrganizationId(UUID id,UUID organizationId);Optional<Delivery> findByOrderIdAndOrganizationId(UUID orderId,UUID organizationId);boolean existsByOrderId(UUID orderId);}
