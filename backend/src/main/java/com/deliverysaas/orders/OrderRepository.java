package com.deliverysaas.orders;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.deliverysaas.orders.domain.Order;
public interface OrderRepository extends JpaRepository<Order,UUID>{
 List<Order> findAllByOrganizationId(UUID organizationId);
 List<Order> findAllByOrganizationIdAndCustomerUserId(UUID organizationId,UUID userId);
 Optional<Order> findByIdAndOrganizationId(UUID id,UUID organizationId);
}
