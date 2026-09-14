package com.deliverysaas.deliveries;
import java.util.*;import org.springframework.data.jpa.repository.JpaRepository;import com.deliverysaas.deliveries.domain.DeliveryStatusHistory;
public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory,UUID>{List<DeliveryStatusHistory> findAllByDeliveryIdOrderByCreatedAtAsc(UUID deliveryId);}
