package com.deliverysaas.deliveries.routing;

import java.util.List;
import java.util.UUID;

import com.deliverysaas.shared.geo.GeoPoint;

public record DeliveryRouteResponse(UUID deliveryId, List<GeoPoint> waypoints, double distanceKm) {
}
