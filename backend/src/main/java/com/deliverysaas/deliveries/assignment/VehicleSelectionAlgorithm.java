package com.deliverysaas.deliveries.assignment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.deliverysaas.vehicles.domain.Vehicle;
import com.deliverysaas.vehicles.domain.VehicleStatus;

public class VehicleSelectionAlgorithm {
        public static Optional<Vehicle> best(List<Vehicle> candidates, BigDecimal totalWeight) {
            Vehicle best = null;
            for (Vehicle vehicle: candidates) {
                if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
                    continue;
                }
                if (totalWeight.compareTo(vehicle.getCapacity()) > 0) {
                    continue;
                }
                if (best == null) {
                    best = vehicle;
                }
                else {
                    if (vehicle.getCapacity().compareTo(best.getCapacity()) < 0) {
                        best = vehicle;
                    }
                }   
            }
            return Optional.ofNullable(best);
        }
}
