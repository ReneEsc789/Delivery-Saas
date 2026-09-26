package com.deliverysaas.deliveries.scheduling;

import java.time.Instant;
import java.util.UUID;

public record TimeWindow(UUID deliveryId, Instant start, Instant end) {
}
