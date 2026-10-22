package com.deliverysaas.deliveries.prioritization;

import java.util.UUID;

public record SkippedDelivery(UUID deliveryId, String reason) {
}
