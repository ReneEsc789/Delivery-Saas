package com.deliverysaas.deliveries.assignment;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.deliverysaas.drivers.domain.Driver;
import com.deliverysaas.drivers.domain.DriverStatus;
import com.deliverysaas.shared.geo.Haversine;

public class DriverAssignmentAlgorithm {

    private static final double max_distance = 20.0;
    private static final Duration max_location = Duration.ofMinutes(30);
    private static final double DISTANCE_WEIGHT = 0.7;
    private static final double WORKLOAD_WEIGHT = 0.3;

    public static Optional<Driver> selectBest(
            List<DriverCandidate> candidates,
            double warehouseLat,
            double warehouseLon,
            Instant now) {

        List<Eligible> eligible = new ArrayList<>();
        long maxDeliveries = 0;

        for (DriverCandidate candidate : candidates) {
            if (candidate.driver().getStatus() != DriverStatus.AVAILABLE) {
                continue;
            }
            if (candidate.location().isBefore(now.minus(max_location))) {
                continue;
            }
            double distance = Haversine.distanceKm(
                    candidate.latitude(), candidate.longitude(), warehouseLat, warehouseLon);
            if (distance > max_distance) {
                continue;
            }
            eligible.add(new Eligible(candidate, distance));
            maxDeliveries = Math.max(maxDeliveries, candidate.deliveries());
        }

        Driver best = null;
        double bestScore = 0;
        for (Eligible e : eligible) {
            double distanceScore = e.distanceKm() / max_distance;
            double workloadScore = maxDeliveries == 0
                    ? 0
                    : (double) e.candidate().deliveries() / maxDeliveries;
            double score = DISTANCE_WEIGHT * distanceScore + WORKLOAD_WEIGHT * workloadScore;

            if (best == null || score < bestScore) {
                best = e.candidate().driver();
                bestScore = score;
            }
        }

        return Optional.ofNullable(best);
    }

    private record Eligible(DriverCandidate candidate, double distanceKm) {}
}
