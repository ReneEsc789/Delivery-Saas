package com.deliverysaas.orders;
import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record CreateOrderRequest(
    @NotNull UUID customerId,
    UUID warehouseId,
    @NotBlank @Size(max=255) String deliveryAddress,
    @Size(max=100) String deliveryCity,
    @Size(max=100) String deliveryState,
    @Size(max=20) String deliveryPostalCode,
    @NotNull @DecimalMin("-90.000000") @DecimalMax("90.000000") BigDecimal deliveryLatitude,
    @NotNull @DecimalMin("-180.000000") @DecimalMax("180.000000") BigDecimal deliveryLongitude,
    @Size(max=2000) String notes,
    @NotEmpty List<@Valid OrderItemRequest> items){}
