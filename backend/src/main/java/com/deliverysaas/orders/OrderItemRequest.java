package com.deliverysaas.orders;
import java.util.UUID;import jakarta.validation.constraints.*;
public record OrderItemRequest(@NotNull UUID productId,@Min(1) int quantity){}
