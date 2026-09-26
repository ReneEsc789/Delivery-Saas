package com.deliverysaas.orders.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.deliverysaas.shared.geo.Haversine;
import com.deliverysaas.warehouses.domain.Warehouse;
import com.deliverysaas.warehouses.domain.WarehouseStatus;

public class WarehouseSelectionAlgorithm {

    private static final double distanceWeight = 0.6;
    private static final double loadWeight = 0.4;

    public static Optional<Warehouse> selectBest(
            List<WarehouseCandidate> candidates,
            Map<UUID, Integer> requested,
            double destinationLat,
            double destinationLon) {

        List<Eligible> eligible = new ArrayList<>();
        double maxDistance = 0;
        long maxOrders = 0;

        for (WarehouseCandidate candidate : candidates) {
            Warehouse warehouse = candidate.warehouse();
            if (warehouse.getStatus() != WarehouseStatus.ACTIVE) {
                continue;
            }
            if (!hasStock(candidate.stock(), requested)) {
                continue;
            }
            double distance = Haversine.distanceKm(
                    warehouse.getLatitude().doubleValue(), warehouse.getLongitude().doubleValue(),
                    destinationLat, destinationLon);
            eligible.add(new Eligible(candidate, distance));
            maxDistance = Math.max(maxDistance, distance);
            maxOrders = Math.max(maxOrders, candidate.activeOrders());
        }

        Warehouse best = null;
        double bestScore = 0;
        for (Eligible e : eligible) {
            double distanceScore = maxDistance == 0 ? 0 : e.distanceKm() / maxDistance;
            double loadScore = maxOrders == 0 ? 0 : (double) e.candidate().activeOrders() / maxOrders;
            double score = distanceWeight * distanceScore + loadWeight * loadScore;

            if (best == null || score < bestScore) {
                best = e.candidate().warehouse();
                bestScore = score;
            }
        }

        return Optional.ofNullable(best);
    }

    private static boolean hasStock(Map<UUID, Integer> stock, Map<UUID, Integer> requested) {
        for (Map.Entry<UUID, Integer> item : requested.entrySet()) {
            if (stock.getOrDefault(item.getKey(), 0) < item.getValue()) {
                return false;
            }
        }
        return true;
    }

    private record Eligible(WarehouseCandidate candidate, double distanceKm) {}
}
