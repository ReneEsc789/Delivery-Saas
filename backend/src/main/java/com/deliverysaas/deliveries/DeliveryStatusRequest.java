package com.deliverysaas.deliveries;
import com.deliverysaas.deliveries.domain.DeliveryStatus;import jakarta.validation.constraints.*;
public record DeliveryStatusRequest(@NotNull DeliveryStatus status,@Size(max=2000) String notes){}
