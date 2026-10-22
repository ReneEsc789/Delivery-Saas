package com.deliverysaas.deliveries.assignment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverysaas.deliveries.AssignDeliveryRequest;
import com.deliverysaas.deliveries.DeliveryRepository;
import com.deliverysaas.deliveries.DeliveryResponse;
import com.deliverysaas.deliveries.DeliveryService;
import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.deliveries.domain.DeliveryStatus;
import com.deliverysaas.drivers.DriverLocationRepository;
import com.deliverysaas.drivers.DriverRepository;
import com.deliverysaas.drivers.domain.Driver;
import com.deliverysaas.drivers.domain.DriverLocation;
import com.deliverysaas.drivers.domain.DriverStatus;
import com.deliverysaas.orders.OrderItemRepository;
import com.deliverysaas.orders.domain.OrderItem;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.vehicles.VehicleRepository;
import com.deliverysaas.vehicles.domain.Vehicle;
import com.deliverysaas.warehouses.domain.Warehouse;

@Service
public class AssignDeliveryService {
    private final DeliveryRepository deliveries;
    private final OrderItemRepository orderItems;
    private final VehicleRepository vehicles;
    private final DriverRepository drivers;
    private final DriverLocationRepository locations;
    private final DeliveryService deliveryService;

    public AssignDeliveryService(DeliveryRepository deliveries,
                                 OrderItemRepository orderItems,
                                 VehicleRepository vehicles,
                                 DriverRepository drivers,
                                 DriverLocationRepository locations,
                                 DeliveryService deliveryService) {
        this.deliveries = deliveries;
        this.orderItems = orderItems;
        this.vehicles = vehicles;
        this.drivers = drivers;
        this.locations = locations;
        this.deliveryService = deliveryService;
    }

    @Transactional
    public DeliveryResponse autoAssign(AuthPrincipal principal, UUID deliveryId) {
        principal.requireManager();
        UUID organizationId = principal.organizationId();

        Delivery delivery = deliveries.findByIdAndOrganizationId(deliveryId, organizationId)
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new ConflictException("Only pending deliveries can be assigned");
        }

        BigDecimal weight = totalWeight(delivery.getOrder().getId());
        Vehicle vehicle = VehicleSelectionAlgorithm.best(vehicles.findAllByOrganizationId(organizationId), weight)
                .orElseThrow(() -> new ConflictException("No available vehicle can carry this order"));

        Warehouse warehouse = delivery.getOrder().getWarehouse();
        Instant now = Instant.now();
        Driver driver = DriverAssignmentAlgorithm.selectBest(
                        candidates(organizationId, now),
                        warehouse.getLatitude().doubleValue(),
                        warehouse.getLongitude().doubleValue(),
                        now)
                .orElseThrow(() -> new ConflictException("No available driver nearby"));

        return deliveryService.assign(principal, deliveryId,
                new AssignDeliveryRequest(driver.getId(), vehicle.getId()));
    }

    private BigDecimal totalWeight(UUID orderId) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : orderItems.findAllByOrderId(orderId)) {
            BigDecimal weight = item.getProduct().getWeight();
            if (weight == null) {
                throw new ConflictException("Product " + item.getProduct().getName() + " has no weight");
            }
            total = total.add(weight.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    private List<DriverCandidate> candidates(UUID organizationId, Instant now) {
        Instant startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        List<DriverCandidate> result = new ArrayList<>();

        for (Driver driver : drivers.findAllByUserOrganizationId(organizationId)) {
            if (driver.getStatus() != DriverStatus.AVAILABLE) {
                continue;
            }
            Optional<DriverLocation> location = locations.findFirstByDriverIdOrderByRecordedAtDesc(driver.getId());
            if (location.isEmpty()) {
                continue;
            }
            long deliveredToday = deliveries.countByDriverIdAndStatusAndDeliveredAtGreaterThanEqual(
                    driver.getId(), DeliveryStatus.DELIVERED, startOfDay);

            result.add(new DriverCandidate(
                    driver,
                    location.get().getLatitude().doubleValue(),
                    location.get().getLongitude().doubleValue(),
                    location.get().getRecordedAt(),
                    deliveredToday));
        }
        return result;
    }
}
