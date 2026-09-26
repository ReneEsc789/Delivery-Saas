package com.deliverysaas.deliveries.routing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverysaas.deliveries.DeliveryRepository;
import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.deliveries.domain.DeliveryStatus;
import com.deliverysaas.orders.domain.Order;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.shared.geo.GeoPoint;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.warehouses.domain.Warehouse;

@Service
public class RoutePlanService {

    private final DeliveryRepository deliveries;

    public RoutePlanService(DeliveryRepository deliveries) {
        this.deliveries = deliveries;
    }

    @Transactional(readOnly = true)
    public RoutePlanResponse plan(AuthPrincipal principal, RoutePlanRequest request) {
        principal.requireManager();

        List<Delivery> stops = new ArrayList<>();
        Warehouse warehouse = null;
        for (UUID id : new LinkedHashSet<>(request.deliveryIds())) {
            Delivery delivery = deliveries.findByIdAndOrganizationId(id, principal.organizationId())
                    .orElseThrow(() -> new NotFoundException("Delivery not found"));
            if (delivery.getStatus() != DeliveryStatus.PENDING) {
                throw new ConflictException("Only pending deliveries can be planned");
            }
            Order order = delivery.getOrder();
            if (order.getDeliveryLatitude() == null || order.getDeliveryLongitude() == null) {
                throw new ConflictException("Delivery " + id + " has no delivery coordinates");
            }
            if (warehouse == null) {
                warehouse = order.getWarehouse();
            } else if (!warehouse.getId().equals(order.getWarehouse().getId())) {
                throw new ConflictException("All deliveries must leave from the same warehouse");
            }
            stops.add(delivery);
        }

        List<GeoPoint> points = new ArrayList<>();
        points.add(GeoPoint.of(warehouse.getLatitude(), warehouse.getLongitude()));
        for (Delivery stop : stops) {
            points.add(GeoPoint.of(stop.getOrder().getDeliveryLatitude(), stop.getOrder().getDeliveryLongitude()));
        }

        double[][] distances = DistanceMatrix.of(points);
        List<Integer> nearest = NearestNeighborAlgorithm.buildRoute(distances);
        List<Integer> optimized = TwoOptAlgorithm.improve(nearest, distances);

        double nearestKm = DistanceMatrix.pathLength(nearest, distances);
        double optimizedKm = DistanceMatrix.pathLength(optimized, distances);
        return new RoutePlanResponse(
                toDeliveryIds(nearest, stops), nearestKm,
                toDeliveryIds(optimized, stops), optimizedKm,
                nearestKm - optimizedKm);
    }

    private List<UUID> toDeliveryIds(List<Integer> route, List<Delivery> stops) {
        List<UUID> ids = new ArrayList<>();
        for (int i = 1; i < route.size(); i++) {
            ids.add(stops.get(route.get(i) - 1).getId());
        }
        return ids;
    }
}
