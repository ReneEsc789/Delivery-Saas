package com.deliverysaas.deliveries.prioritization;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverysaas.deliveries.DeliveryRepository;
import com.deliverysaas.deliveries.DeliveryResponse;
import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.deliveries.domain.DeliveryStatus;
import com.deliverysaas.shared.security.AuthPrincipal;

@Service
public class DeliveryPriorityService {

    private final DeliveryRepository deliveries;

    public DeliveryPriorityService(DeliveryRepository deliveries) {
        this.deliveries = deliveries;
    }

    @Transactional(readOnly = true)
    public List<PrioritizedDeliveryResponse> queue(AuthPrincipal principal) {
        principal.requireManager();
        Instant now = Instant.now();

        DeliveryPriorityQueue queue = new DeliveryPriorityQueue();
        for (Delivery delivery : deliveries.findAllByOrganizationIdAndStatus(
                principal.organizationId(), DeliveryStatus.PENDING)) {
            queue.add(new PrioritizedDelivery(delivery, DeliveryPriorityCalculator.score(delivery, now)));
        }

        List<PrioritizedDeliveryResponse> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            PrioritizedDelivery next = queue.poll();
            result.add(new PrioritizedDeliveryResponse(DeliveryResponse.from(next.delivery()), next.score()));
        }
        return result;
    }
}
