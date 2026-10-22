package com.deliverysaas.deliveries.prioritization;

import com.deliverysaas.deliveries.DeliveryResponse;

public record PrioritizedDeliveryResponse(DeliveryResponse delivery, double score) {
}
