package com.deliverysaas.orders;
import java.math.BigDecimal;import java.util.UUID;import com.deliverysaas.orders.domain.OrderItem;
public record OrderItemResponse(UUID id,UUID productId,String productName,int quantity,BigDecimal unitPrice,BigDecimal subtotal){public static OrderItemResponse from(OrderItem i){return new OrderItemResponse(i.getId(),i.getProduct().getId(),i.getProduct().getName(),i.getQuantity(),i.getUnitPrice(),i.getSubtotal());}}
