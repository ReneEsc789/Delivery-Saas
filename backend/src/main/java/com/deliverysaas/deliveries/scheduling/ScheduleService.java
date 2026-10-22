package com.deliverysaas.deliveries.scheduling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deliverysaas.deliveries.DeliveryRepository;
import com.deliverysaas.deliveries.domain.Delivery;
import com.deliverysaas.deliveries.domain.DeliveryStatus;
import com.deliverysaas.shared.error.ConflictException;
import com.deliverysaas.shared.error.NotFoundException;
import com.deliverysaas.shared.security.AuthPrincipal;

@Service
public class ScheduleService {

    private final DeliveryRepository deliveries;

    public ScheduleService(DeliveryRepository deliveries) {
        this.deliveries = deliveries;
    }

    @Transactional(readOnly = true)
    public SchedulePlanResponse plan(AuthPrincipal principal, SchedulePlanRequest request) {
        principal.requireManager();

        List<TimeWindow> windows = new ArrayList<>();
        List<UUID> withoutWindow = new ArrayList<>();
        for (UUID id : new LinkedHashSet<>(request.deliveryIds())) {
            Delivery delivery = deliveries.findByIdAndOrganizationId(id, principal.organizationId())
                    .orElseThrow(() -> new NotFoundException("Delivery not found"));
            if (delivery.getStatus() != DeliveryStatus.PENDING) {
                throw new ConflictException("Only pending deliveries can be planned");
            }
            if (delivery.getWindowStart() == null || delivery.getWindowEnd() == null) {
                withoutWindow.add(id);
            } else {
                windows.add(new TimeWindow(id, delivery.getWindowStart(), delivery.getWindowEnd()));
            }
        }

        List<TimeWindow> schedule = IntervalSchedulingAlgorithm.selectCompatible(windows);
        Set<UUID> scheduled = new HashSet<>();
        for (TimeWindow window : schedule) {
            scheduled.add(window.deliveryId());
        }
        List<UUID> conflicting = new ArrayList<>();
        for (TimeWindow window : windows) {
            if (!scheduled.contains(window.deliveryId())) {
                conflicting.add(window.deliveryId());
            }
        }
        return new SchedulePlanResponse(schedule, conflicting, withoutWindow);
    }
}
