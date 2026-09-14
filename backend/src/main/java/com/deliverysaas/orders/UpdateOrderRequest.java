package com.deliverysaas.orders;
import java.util.*;import jakarta.validation.Valid;import jakarta.validation.constraints.*;
public record UpdateOrderRequest(@NotNull UUID warehouseId,@NotBlank @Size(max=255) String deliveryAddress,
 @Size(max=100) String deliveryCity,@Size(max=100) String deliveryState,@Size(max=20) String deliveryPostalCode,
 @Size(max=2000) String notes,@NotEmpty List<@Valid OrderItemRequest> items){}
