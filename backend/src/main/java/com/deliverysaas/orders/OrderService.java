package com.deliverysaas.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverysaas.audit.AuditService;
import com.deliverysaas.customers.CustomerRepository;
import com.deliverysaas.customers.domain.Customer;
import com.deliverysaas.deliveries.DeliveryRepository;
import com.deliverysaas.deliveries.DeliveryStatusHistoryRepository;
import com.deliverysaas.deliveries.domain.DeliveryStatus;
import com.deliverysaas.deliveries.domain.DeliveryStatusHistory;
import com.deliverysaas.drivers.domain.DriverStatus;
import com.deliverysaas.orders.domain.Order;
import com.deliverysaas.orders.domain.OrderItem;
import com.deliverysaas.orders.domain.OrderStatus;
import com.deliverysaas.orders.selection.WarehouseSelectionService;
import com.deliverysaas.organizations.OrganizationRepository;
import com.deliverysaas.products.ProductRepository;
import com.deliverysaas.products.domain.Product;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.ForbiddenException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.shared.security.AuthPrincipal;
import com.deliverysaas.users.UserRepository;
import com.deliverysaas.users.domain.UserRole;
import com.deliverysaas.users.domain.UserStatus;
import com.deliverysaas.vehicles.domain.VehicleStatus;
import com.deliverysaas.warehouses.WarehouseInventoryRepository;
import com.deliverysaas.warehouses.WarehouseRepository;
import com.deliverysaas.warehouses.domain.Warehouse;
import com.deliverysaas.warehouses.domain.WarehouseInventory;
import com.deliverysaas.warehouses.domain.WarehouseStatus;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final CustomerRepository customers;
    private final WarehouseRepository warehouses;
    private final ProductRepository products;
    private final WarehouseInventoryRepository inventory;
    private final OrganizationRepository organizations;
    private final DeliveryRepository deliveries;
    private final DeliveryStatusHistoryRepository deliveryHistory;
    private final UserRepository users;
    private final AuditService audit;
    private final WarehouseSelectionService warehouseSelection;

    public OrderService(OrderRepository orders, OrderItemRepository items, CustomerRepository customers,
            WarehouseRepository warehouses, ProductRepository products, WarehouseInventoryRepository inventory,
            OrganizationRepository organizations, DeliveryRepository deliveries,
            DeliveryStatusHistoryRepository deliveryHistory, UserRepository users, AuditService audit,
            WarehouseSelectionService warehouseSelection) {
        this.orders = orders;
        this.items = items;
        this.customers = customers;
        this.warehouses = warehouses;
        this.products = products;
        this.inventory = inventory;
        this.organizations = organizations;
        this.deliveries = deliveries;
        this.deliveryHistory = deliveryHistory;
        this.users = users;
        this.audit = audit;
        this.warehouseSelection = warehouseSelection;
    }

    @Transactional
    public OrderResponse create(AuthPrincipal a, CreateOrderRequest r) {
        Customer c = customer(r.customerId(), a.organizationId());
        authorizeCustomer(a, c);
        if (c.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new ConflictException("Customer is inactive");
        }
        Warehouse w = r.warehouseId() != null
            ? warehouse(r.warehouseId(), a.organizationId())
            : warehouseSelection.select(a.organizationId(), r.items(), r.deliveryLatitude(), r.deliveryLongitude());
        active(w);
        Order o = new Order(organizations.getReferenceById(a.organizationId()), c, w, r.deliveryAddress().trim());
        apply(o, r.deliveryAddress(), r.deliveryCity(), r.deliveryState(), r.deliveryPostalCode(), r.notes());
        o.setDeliveryLatitude(r.deliveryLatitude());
        o.setDeliveryLongitude(r.deliveryLongitude());
        orders.save(o);
        replaceItems(o, w, r.items());
        audit.record("CREATE", "ORDER", o.getId(), "Order created");
        return response(o);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> all(AuthPrincipal a) {
        if (a.role() == UserRole.DRIVER) {
            throw new ForbiddenException("Drivers cannot access orders");
        }
        List<Order> list = a.role() == UserRole.CUSTOMER
            ? orders.findAllByOrganizationIdAndCustomerUserId(a.organizationId(), a.userId())
            : orders.findAllByOrganizationId(a.organizationId());
        return list.stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse one(AuthPrincipal a, UUID id) {
        Order o = get(id, a.organizationId());
        authorizeCustomer(a, o.getCustomer());
        return response(o);
    }

    @Transactional
    public OrderResponse update(AuthPrincipal a, UUID id, UpdateOrderRequest r) {
        Order o = get(id, a.organizationId());
        authorizeCustomer(a, o.getCustomer());
        if (o.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only pending orders can be edited");
        }
        Warehouse w = warehouse(r.warehouseId(), a.organizationId());
        active(w);
        o.setWarehouse(w);
        apply(o, r.deliveryAddress(), r.deliveryCity(), r.deliveryState(), r.deliveryPostalCode(), r.notes());
        o.setDeliveryLatitude(r.deliveryLatitude());
        o.setDeliveryLongitude(r.deliveryLongitude());
        replaceItems(o, w, r.items());
        audit.record("UPDATE", "ORDER", o.getId(), "Order updated");
        return response(o);
    }

    @Transactional
    public OrderResponse confirm(AuthPrincipal a, UUID id) {
        a.requireManager();
        Order o = get(id, a.organizationId());
        if (o.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only pending orders can be confirmed");
        }
        for (OrderItem item : items.findAllByOrderId(id)) {
            WarehouseInventory stock = inventory.findForUpdate(o.getWarehouse().getId(), item.getProduct().getId())
                .orElseThrow(() -> new ConflictException("Product is not stocked in this warehouse"));
            if (stock.getQuantity() < item.getQuantity()) {
                throw new ConflictException("Insufficient inventory for " + item.getProduct().getName());
            }
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
        }
        o.setStatus(OrderStatus.CONFIRMED);
        audit.record("CONFIRM", "ORDER", o.getId(), "Order confirmed and inventory deducted");
        return response(o);
    }

    @Transactional
    public OrderResponse changeStatus(AuthPrincipal a, UUID id, OrderStatusRequest r) {
        a.requireManager();
        Order o = get(id, a.organizationId());
        OrderStatus expected = switch (r.status()) {
            case PREPARING -> OrderStatus.CONFIRMED;
            case READY -> OrderStatus.PREPARING;
            default -> null;
        };
        if (expected == null || o.getStatus() != expected) {
            throw new ConflictException("Invalid order status transition");
        }
        o.setStatus(r.status());
        audit.record("STATUS_CHANGE", "ORDER", o.getId(), "Order status changed to " + r.status());
        return response(o);
    }

    @Transactional
    public void cancel(AuthPrincipal a, UUID id) {
        Order o = get(id, a.organizationId());
        authorizeCustomer(a, o.getCustomer());
        if (o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.COMPLETED) {
            throw new ConflictException("Order cannot be cancelled");
        }
        deliveries.findByOrderIdAndOrganizationId(o.getId(), a.organizationId()).ifPresent(d -> {
            if (d.getStatus() == DeliveryStatus.PICKED_UP || d.getStatus() == DeliveryStatus.IN_TRANSIT
                    || d.getStatus() == DeliveryStatus.DELIVERED) {
                throw new ConflictException("Order cannot be cancelled after pickup");
            }
            DeliveryStatus previous = d.getStatus();
            d.setStatus(DeliveryStatus.CANCELLED);
            if (d.getDriver() != null) {
                d.getDriver().setStatus(DriverStatus.AVAILABLE);
            }
            if (d.getVehicle() != null) {
                d.getVehicle().setStatus(VehicleStatus.AVAILABLE);
            }
            DeliveryStatusHistory h = new DeliveryStatusHistory(d, DeliveryStatus.CANCELLED,
                users.getReferenceById(a.userId()));
            h.setPreviousStatus(previous);
            h.setNotes("Order cancelled");
            deliveryHistory.save(h);
            audit.record("CANCEL", "DELIVERY", d.getId(), "Delivery cancelled with order");
        });
        if (o.getStatus() != OrderStatus.PENDING) {
            restore(o);
        }
        o.setStatus(OrderStatus.CANCELLED);
        audit.record("CANCEL", "ORDER", o.getId(), "Order cancelled");
    }

    private void replaceItems(Order o, Warehouse w, List<OrderItemRequest> req) {
        Set<UUID> seen = new HashSet<>();
        List<OrderItem> result = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest r : req) {
            if (!seen.add(r.productId())) {
                throw new ConflictException("Products cannot be duplicated");
            }
            Product p = products.findByIdAndOrganizationId(r.productId(), o.getOrganization().getId())
                .orElseThrow(() -> new NotFoundException("Product not found"));
            if (!p.isActive()) {
                throw new ConflictException("Product is inactive");
            }
            if (inventory.findByWarehouseIdAndProductId(w.getId(), p.getId()).isEmpty()) {
                throw new ConflictException("Product is not stocked in this warehouse");
            }
            BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(r.quantity()));
            result.add(new OrderItem(o, p, r.quantity(), p.getPrice(), sub));
            total = total.add(sub);
        }
        items.deleteAllByOrderId(o.getId());
        items.saveAll(result);
        o.setTotalAmount(total);
    }

    private void restore(Order o) {
        for (OrderItem item : items.findAllByOrderId(o.getId())) {
            WarehouseInventory stock = inventory.findForUpdate(o.getWarehouse().getId(), item.getProduct().getId())
                .orElseThrow(() -> new ConflictException("Inventory record not found"));
            stock.setQuantity(stock.getQuantity() + item.getQuantity());
        }
    }

    private OrderResponse response(Order o) {
        return OrderResponse.from(o, items.findAllByOrderId(o.getId()));
    }

    private Order get(UUID id, UUID org) {
        return orders.findByIdAndOrganizationId(id, org)
            .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    private Customer customer(UUID id, UUID org) {
        return customers.findByIdAndUserOrganizationId(id, org)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private Warehouse warehouse(UUID id, UUID org) {
        return warehouses.findByIdAndOrganizationId(id, org)
            .orElseThrow(() -> new NotFoundException("Warehouse not found"));
    }

    private void active(Warehouse w) {
        if (w.getStatus() != WarehouseStatus.ACTIVE) {
            throw new ConflictException("Warehouse is inactive");
        }
    }

    private void authorizeCustomer(AuthPrincipal a, Customer c) {
        if (a.role() == UserRole.DRIVER) {
            throw new ForbiddenException("Drivers cannot access orders");
        }
        if (a.role() == UserRole.CUSTOMER && !a.userId().equals(c.getUser().getId())) {
            throw new NotFoundException("Order not found");
        }
    }

    private void apply(Order o, String address, String city, String state, String postalCode, String notes) {
        o.setDeliveryAddress(address.trim());
        o.setDeliveryCity(trim(city));
        o.setDeliveryState(trim(state));
        o.setDeliveryPostalCode(trim(postalCode));
        o.setNotes(trim(notes));
    }

    private String trim(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}