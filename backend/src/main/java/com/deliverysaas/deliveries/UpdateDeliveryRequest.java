package com.deliverysaas.deliveries;
import java.time.Instant;import com.deliverysaas.deliveries.domain.DeliveryPriority;import jakarta.validation.constraints.NotNull;
public record UpdateDeliveryRequest(@NotNull DeliveryPriority priority,Instant scheduledAt,Instant windowStart,Instant windowEnd){}
