package com.deliverysaas.drivers;
import java.math.BigDecimal;import jakarta.validation.constraints.*;
public record CreateDriverLocationRequest(@NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,@NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude){}
