package com.deliverysaas.deliveries;
import java.util.UUID;import jakarta.validation.constraints.NotNull;
public record AssignDeliveryRequest(@NotNull UUID driverId,@NotNull UUID vehicleId){}
