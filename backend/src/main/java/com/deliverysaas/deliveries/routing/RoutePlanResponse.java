package com.deliverysaas.deliveries.routing;

import java.util.List;
import java.util.UUID;

public record RoutePlanResponse(
        List<UUID> nearestNeighborOrder,
        double nearestNeighborKm,
        List<UUID> optimizedOrder,
        double optimizedKm,
        double savedKm) {
}
