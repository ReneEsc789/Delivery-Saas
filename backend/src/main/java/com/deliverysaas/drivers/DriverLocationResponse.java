package com.deliverysaas.drivers;
import java.math.BigDecimal;import java.time.Instant;import java.util.UUID;import com.deliverysaas.drivers.domain.DriverLocation;
public record DriverLocationResponse(UUID id,UUID driverId,BigDecimal latitude,BigDecimal longitude,Instant recordedAt){public static DriverLocationResponse from(DriverLocation l){return new DriverLocationResponse(l.getId(),l.getDriver().getId(),l.getLatitude(),l.getLongitude(),l.getRecordedAt());}}
