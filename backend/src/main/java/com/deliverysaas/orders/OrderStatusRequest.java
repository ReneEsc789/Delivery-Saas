package com.deliverysaas.orders;
import com.deliverysaas.orders.domain.OrderStatus;import jakarta.validation.constraints.NotNull;
public record OrderStatusRequest(@NotNull OrderStatus status){}
