package com.deliverysaas.deliveries.prioritization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.deliverysaas.deliveries.DeliveryResponse;
import com.deliverysaas.deliveries.assignment.AssignDeliveryService;
import com.deliverysaas.shared.error.ApiException;
import com.deliverysaas.shared.security.AuthPrincipal;

// Sin @Transactional a propósito: cada autoAssign corre en su propia transacción,
// así un fallo no revierte las asignaciones que ya salieron bien.
@Service
public class PendingDispatchService {

    private final DeliveryPriorityService priority;
    private final AssignDeliveryService assign;

    public PendingDispatchService(DeliveryPriorityService priority, AssignDeliveryService assign) {
        this.priority = priority;
        this.assign = assign;
    }

    public DispatchResponse autoAssignPending(AuthPrincipal principal) {
        List<DeliveryResponse> assigned = new ArrayList<>();
        List<SkippedDelivery> skipped = new ArrayList<>();

        for (PrioritizedDeliveryResponse next : priority.queue(principal)) {
            UUID deliveryId = next.delivery().id();
            try {
                assigned.add(assign.autoAssign(principal, deliveryId));
            } catch (ApiException e) {
                skipped.add(new SkippedDelivery(deliveryId, e.getMessage()));
            }
        }
        return new DispatchResponse(assigned, skipped);
    }
}
