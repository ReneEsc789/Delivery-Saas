package com.deliverysaas.deliveries.routing;

import java.util.List;

public record RouteResult(List<Integer> nodes, double distanceKm) {
}
