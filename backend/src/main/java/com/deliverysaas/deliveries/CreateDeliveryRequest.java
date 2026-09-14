package com.deliverysaas.deliveries;
import java.time.Instant;import java.util.UUID;import com.deliverysaas.deliveries.domain.DeliveryPriority;import jakarta.validation.constraints.*;
public record CreateDeliveryRequest(@NotNull UUID orderId,@NotNull DeliveryPriority priority,Instant scheduledAt,Instant windowStart,Instant windowEnd){}
