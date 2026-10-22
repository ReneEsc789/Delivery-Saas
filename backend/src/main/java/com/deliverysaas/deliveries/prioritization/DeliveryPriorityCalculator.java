package com.deliverysaas.deliveries.prioritization;

import java.time.Duration;
import java.time.Instant;

import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.orders.domain.Order;
import com.deliverysaas.shared.geo.Haversine;

public class DeliveryPriorityCalculator {

    private static final double maxLatenessPoints = 80;
    private static final double windowHorizonMinutes = 240;
    private static final double maxWaitingPoints = 60;
    private static final double minutesPerWaitingPoint = 3;
    private static final double maxDistancePoints = 10;
    private static final double kmPerDistancePoint = 2;

    public static double score(Delivery delivery, Instant now) {
        return urgency(delivery) + lateness(delivery, now) + closeness(delivery);
    }

    private static double urgency(Delivery delivery) {
        return switch (delivery.getPriority()) {
            case URGENT -> 100;
            case HIGH -> 60;
            case NORMAL -> 30;
            case LOW -> 0;
        };
    }

    private static double lateness(Delivery delivery, Instant now) {
        if (delivery.getWindowEnd() != null) {
            double minutesLeft = Duration.between(now, delivery.getWindowEnd()).toMinutes();
            double points = maxLatenessPoints * (1 - minutesLeft / windowHorizonMinutes);
            return Math.max(0, Math.min(maxLatenessPoints, points));
        }
        double minutesWaiting = Duration.between(delivery.getCreatedAt(), now).toMinutes();
        return Math.min(maxWaitingPoints, minutesWaiting / minutesPerWaitingPoint);
    }

    private static double closeness(Delivery delivery) {
        Order order = delivery.getOrder();
        if (order.getDeliveryLatitude() == null || order.getDeliveryLongitude() == null) {
            return 0;
        }
        double km = Haversine.distanceKm(
                order.getWarehouse().getLatitude().doubleValue(),
                order.getWarehouse().getLongitude().doubleValue(),
                order.getDeliveryLatitude().doubleValue(),
                order.getDeliveryLongitude().doubleValue());
        return Math.max(0, maxDistancePoints - km / kmPerDistancePoint);
    }
}