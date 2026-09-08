package com.deliverysaas.products;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(max = 80) String sku,
    @Size(max = 2000) String description,
    @NotNull @DecimalMin(value = "0.00") BigDecimal price,
    @DecimalMin(value = "0.00") BigDecimal weight
) {}
