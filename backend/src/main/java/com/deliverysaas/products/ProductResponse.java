package com.deliverysaas.products;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.deliverysaas.products.domain.Product;

public record ProductResponse(
    UUID id,
    String name,
    String sku,
    String description,
    BigDecimal price,
    BigDecimal weight,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(), product.getName(), product.getSku(), product.getDescription(),
            product.getPrice(), product.getWeight(), product.isActive(),
            product.getCreatedAt(), product.getUpdatedAt());
    }
}
