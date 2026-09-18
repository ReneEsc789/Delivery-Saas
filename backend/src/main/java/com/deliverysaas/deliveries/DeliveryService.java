package com.deliverysaas.deliveries;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.audit.AuditService;
import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.deliveries.domain.DeliveryPriority;
import com.deliverysaas.deliveries.domain.DeliveryStatus;
import com.deliverysaas.deliveries.domain.DeliveryStatusHistory;
import com.deliverysaas.drivers.DriverRepository;
import com.deliverysaas.drivers.domain.Driver;
import com.deliverysaas.drivers.domain.DriverStatus;
import com.deliverysaas.orders.OrderRepository;
import com.deliverysaas.orders.domain.Order;
import com.deliverysaas.orders.domain.OrderStatus;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.UserRepository;
import com.deliverysaas.users.domain.UserRole;
import com.deliverysaas.vehicles.VehicleRepository;
import com.deliverysaas.vehicles.domain.Vehicle;
import com.deliverysaas.vehicles.domain.VehicleStatus;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveries;
    private final DeliveryStatusHistoryRepository history;
    private final OrderRepository orders;
    private final DriverRepository drivers;
    private final VehicleRepository vehicles;
    private final UserRepository users;
    private final AuditService audit;

    public DeliveryService(DeliveryRepository deliveries, DeliveryStatusHistoryRepository history, OrderRepository orders, DriverRepository drivers, VehicleRepository vehicles, UserRepository users, AuditService audit) {
        this.deliveries = deliveries;
        this.history = history;
        this.orders = orders;
        this.drivers = drivers;
        this.vehicles = vehicles;
        this.users = users;
        this.audit = audit;
    }

    @Transactional
    public DeliveryResponse create(AuthPrincipal a, CreateDeliveryRequest r) {
        a.requireManager();
        Order o = orders.findByIdAndOrganizationId(r.orderId(), a.organizationId())
            .orElseThrow(() -> new NotFoundException("Order not found"));
        if (o.getStatus() != OrderStatus.READY) {
            throw new ConflictException("Order must be ready");
        }
        if (deliveries.existsByOrderId(o.getId())) {
            throw new ConflictException("Order already has a delivery");
        }
        validateWindow(r.windowStart(), r.windowEnd());
        Delivery d = new Delivery(o.getOrganization(), o);
        apply(d, r.priority(), r.scheduledAt(), r.windowStart(), r.windowEnd());
        deliveries.save(d);
        record(d, null, DeliveryStatus.PENDING, a.userId(), null);
        audit.record("CREATE", "DELIVERY", d.getId(), "Delivery created");
        return DeliveryResponse.from(d);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> all(AuthPrincipal a) {
        List<Delivery> list;
        if (a.role() == UserRole.DRIVER) {
            list = deliveries.findAllByOrganizationIdAndDriverUserId(a.organizationId(), a.userId());
        } else if (a.role() == UserRole.CUSTOMER) {
            list = deliveries.findAllByOrganizationIdAndOrderCustomerUserId(a.organizationId(), a.userId());
        } else {
            list = deliveries.findAllByOrganizationId(a.organizationId());
        }
        return list.stream().map(DeliveryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DeliveryResponse one(AuthPrincipal a, UUID id) {
        Delivery d = get(id, a.organizationId());
        access(a, d);
        return DeliveryResponse.from(d);
    }

    @Transactional
    public DeliveryResponse update(AuthPrincipal a, UUID id, UpdateDeliveryRequest r) {
        a.requireManager();
        Delivery d = get(id, a.organizationId());
        if (d.getStatus() != DeliveryStatus.PENDING && d.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new ConflictException("Delivery can no longer be edited");
        }
        validateWindow(r.windowStart(), r.windowEnd());
        apply(d, r.priority(), r.scheduledAt(), r.windowStart(), r.windowEnd());
        audit.record("UPDATE", "DELIVERY", d.getId(), "Delivery updated");
        return DeliveryResponse.from(d);
    }

    @Transactional
    public DeliveryResponse assign(AuthPrincipal a, UUID id, AssignDeliveryRequest r) {
        a.requireManager();
        Delivery d = get(id, a.organizationId());
        if (d.getStatus() != DeliveryStatus.PENDING) {
            throw new ConflictException("Only pending deliveries can be assigned");
        }
        Driver driver = drivers.findForUpdate(r.driverId(), a.organizationId())
            .orElseThrow(() -> new NotFoundException("Driver not found"));
        Vehicle vehicle = vehicles.findForUpdate(r.vehicleId(), a.organizationId())
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new ConflictException("Driver is not available");
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new ConflictException("Vehicle is not available");
        }
        d.setDriver(driver);
        d.setVehicle(vehicle);
        driver.setStatus(DriverStatus.BUSY);
        vehicle.setStatus(VehicleStatus.IN_USE);
        transition(d, DeliveryStatus.ASSIGNED, a.userId(), null);
        audit.record("ASSIGN", "DELIVERY", d.getId(), "Delivery assigned");
        return DeliveryResponse.from(d);
    }

    @Transactional
    public DeliveryResponse status(AuthPrincipal a, UUID id, DeliveryStatusRequest r) {
        if (a.role() == UserRole.CUSTOMER) {
            throw new ForbiddenException("Customers cannot update deliveries");
        }
        Delivery d = get(id, a.organizationId());
        access(a, d);
        if (a.role() == UserRole.DRIVER
                && (d.getDriver() == null || !d.getDriver().getUser().getId().equals(a.userId()))) {
            throw new NotFoundException("Delivery not found");
        }
        DeliveryStatus next = r.status();
        if (a.role() == UserRole.DRIVER && next == DeliveryStatus.CANCELLED) {
            throw new ForbiddenException("Drivers cannot cancel deliveries");
        }
        boolean ok = switch (d.getStatus()) {
            case ASSIGNED -> next == DeliveryStatus.PICKED_UP || next == DeliveryStatus.CANCELLED;
            case PICKED_UP -> next == DeliveryStatus.IN_TRANSIT || next == DeliveryStatus.FAILED;
            case IN_TRANSIT -> next == DeliveryStatus.DELIVERED || next == DeliveryStatus.FAILED;
            case PENDING -> next == DeliveryStatus.CANCELLED;
            default -> false;
        };
        if (!ok) {
            throw new ConflictException("Invalid delivery status transition");
        }
        if (next == DeliveryStatus.FAILED && (r.notes() == null || r.notes().isBlank())) {
            throw new ConflictException("Failure reason is required");
        }
        if (next == DeliveryStatus.PICKED_UP) {
            d.setPickedUpAt(Instant.now());
        }
        if (next == DeliveryStatus.DELIVERED) {
            d.setDeliveredAt(Instant.now());
            d.getOrder().setStatus(OrderStatus.COMPLETED);
        }
        if (next == DeliveryStatus.FAILED) {
            d.setFailureReason(r.notes().trim());
        }
        transition(d, next, a.userId(), r.notes());
        if (next == DeliveryStatus.DELIVERED || next == DeliveryStatus.FAILED || next == DeliveryStatus.CANCELLED) {
            release(d);
        }
        audit.record("STATUS_CHANGE", "DELIVERY", d.getId(), "Delivery status changed to " + next);
        return DeliveryResponse.from(d);
    }

    @Transactional
    public void cancel(AuthPrincipal a, UUID id) {
        a.requireManager();
        Delivery d = get(id, a.organizationId());
        if (d.getStatus() != DeliveryStatus.PENDING && d.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new ConflictException("Delivery cannot be cancelled");
        }
        transition(d, DeliveryStatus.CANCELLED, a.userId(), null);
        release(d);
        audit.record("CANCEL", "DELIVERY", d.getId(), "Delivery cancelled");
    }

    @Transactional(readOnly = true)
    public List<DeliveryHistoryResponse> history(AuthPrincipal a, UUID id) {
        Delivery d = get(id, a.organizationId());
        access(a, d);
        return history.findAllByDeliveryIdOrderByCreatedAtAsc(id).stream()
            .map(DeliveryHistoryResponse::from)
            .toList();
    }

    private void transition(Delivery d, DeliveryStatus next, UUID user, String notes) {
        DeliveryStatus previous = d.getStatus();
        d.setStatus(next);
        record(d, previous, next, user, notes);
    }

    private void record(Delivery d, DeliveryStatus previous, DeliveryStatus next, UUID user, String notes) {
        DeliveryStatusHistory h = new DeliveryStatusHistory(d, next, users.getReferenceById(user));
        h.setPreviousStatus(previous);
        h.setNotes(trim(notes));
        history.save(h);
    }

    private void release(Delivery d) {
        if (d.getDriver() != null) {
            d.getDriver().setStatus(DriverStatus.AVAILABLE);
        }
        if (d.getVehicle() != null) {
            d.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        }
    }

    private Delivery get(UUID id, UUID org) {
        return deliveries.findByIdAndOrganizationId(id, org)
            .orElseThrow(() -> new NotFoundException("Delivery not found"));
    }

    private void access(AuthPrincipal a, Delivery d) {
        if (a.role() == UserRole.DRIVER
                && (d.getDriver() == null || !d.getDriver().getUser().getId().equals(a.userId()))) {
            throw new NotFoundException("Delivery not found");
        }
        if (a.role() == UserRole.CUSTOMER && !d.getOrder().getCustomer().getUser().getId().equals(a.userId())) {
            throw new NotFoundException("Delivery not found");
        }
    }

    private void validateWindow(Instant s, Instant e) {
        if (s != null && e != null && !e.isAfter(s)) {
            throw new ConflictException("Window end must be after window start");
        }
    }

    private void apply(Delivery d, DeliveryPriority p, Instant s, Instant ws, Instant we) {
        d.setPriority(p);
        d.setScheduledAt(s);
        d.setWindowStart(ws);
        d.setWindowEnd(we);
    }

    private String trim(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}