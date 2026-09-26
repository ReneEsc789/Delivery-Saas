package com.deliverysaas.deliveries.scheduling;

import java.util.List;
import java.util.UUID;

public record SchedulePlanResponse(List<TimeWindow> schedule, List<UUID> conflicting, List<UUID> withoutWindow) {
}
