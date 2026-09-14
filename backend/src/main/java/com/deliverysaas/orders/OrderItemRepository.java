package com.deliverysaas.orders;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.deliverysaas.orders.domain.OrderItem;
public interface OrderItemRepository extends JpaRepository<OrderItem,UUID>{List<OrderItem> findAllByOrderId(UUID orderId);void deleteAllByOrderId(UUID orderId);}
