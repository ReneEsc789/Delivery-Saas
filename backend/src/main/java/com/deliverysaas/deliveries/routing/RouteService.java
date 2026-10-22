package com.deliverysaas.deliveries.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverysaas.deliveries.DeliveryRepository;
import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.deliveries.domain.DeliveryStatus;
import com.deliverysaas.orders.domain.Order;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.shared.geo.GeoPoint;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.domain.UserRole;
import com.deliverysaas.warehouses.WarehouseRepository;
import com.deliverysaas.warehouses.domain.Warehouse;
import com.deliverysaas.warehouses.domain.WarehouseStatus;

@Service
public class RouteService {

    private static final int neighborsPerNode = 4;
    private static final List<DeliveryStatus> activeStatuses = List.of(
            DeliveryStatus.PENDING, DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT);

    private final DeliveryRepository deliveries;
    private final WarehouseRepository warehouses;

    public RouteService(DeliveryRepository deliveries, WarehouseRepository warehouses) {
        this.deliveries = deliveries;
        this.warehouses = warehouses;
    }

    @Transactional(readOnly = true)
    public DeliveryRouteResponse route(AuthPrincipal principal, UUID deliveryId) {
        UUID organizationId = principal.organizationId();
        Delivery delivery = deliveries.findByIdAndOrganizationId(deliveryId, organizationId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        authorize(principal, delivery);

        Order order = delivery.getOrder();
        if (order.getDeliveryLatitude() == null || order.getDeliveryLongitude() == null) {
            throw new ConflictException("Order has no delivery coordinates");
        }

        List<GeoPoint> points = new ArrayList<>();
        points.add(GeoPoint.of(order.getWarehouse().getLatitude(), order.getWarehouse().getLongitude()));
        points.add(GeoPoint.of(order.getDeliveryLatitude(), order.getDeliveryLongitude()));
        for (Warehouse warehouse : warehouses.findAllByOrganizationId(organizationId)) {
            if (warehouse.getStatus() == WarehouseStatus.ACTIVE
                    && !warehouse.getId().equals(order.getWarehouse().getId())) {
                points.add(GeoPoint.of(warehouse.getLatitude(), warehouse.getLongitude()));
            }
        }
        for (Delivery other : deliveries.findAllByOrganizationIdAndStatusIn(organizationId, activeStatuses)) {
            Order otherOrder = other.getOrder();
            if (!other.getId().equals(delivery.getId())
                    && otherOrder.getDeliveryLatitude() != null && otherOrder.getDeliveryLongitude() != null) {
                points.add(GeoPoint.of(otherOrder.getDeliveryLatitude(), otherOrder.getDeliveryLongitude()));
            }
        }

        Graph graph = buildGraph(points);
        RouteResult result = DijkstraAlgorithm.shortestPath(graph, 0, 1)
                .orElseGet(() -> {
                    graph.connect(0, 1, points.get(0).distanceKmTo(points.get(1)));
                    return DijkstraAlgorithm.shortestPath(graph, 0, 1).orElseThrow();
                });

        List<GeoPoint> waypoints = new ArrayList<>();
        for (int node : result.nodes()) {
            waypoints.add(points.get(node));
        }
        return new DeliveryRouteResponse(deliveryId, waypoints, result.distanceKm());
    }

    private Graph buildGraph(List<GeoPoint> points) {
        Graph graph = new Graph(points.size());
        for (int i = 0; i < points.size(); i++) {
            GeoPoint from = points.get(i);
            List<Integer> others = new ArrayList<>();
            for (int j = 0; j < points.size(); j++) {
                if (j != i) {
                    others.add(j);
                }
            }
            others.sort(Comparator.comparingDouble(j -> from.distanceKmTo(points.get(j))));
            for (int j : others.subList(0, Math.min(neighborsPerNode, others.size()))) {
                graph.connect(i, j, from.distanceKmTo(points.get(j)));
            }
        }
        return graph;
    }

    private void authorize(AuthPrincipal principal, Delivery delivery) {
        if (principal.role() == UserRole.CUSTOMER) {
            throw new ForbiddenException("Customers cannot access delivery routes");
        }
        if (principal.role() == UserRole.DRIVER
                && (delivery.getDriver() == null || !delivery.getDriver().getUser().getId().equals(principal.userId()))) {
            throw new NotFoundException("Delivery not found");
        }
    }
}
