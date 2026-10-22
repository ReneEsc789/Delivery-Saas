package com.deliverysaas.shared.geo;

import java.math.BigDecimal;

public record GeoPoint(double latitude, double longitude) {

    public static GeoPoint of(BigDecimal latitude, BigDecimal longitude) {
        return new GeoPoint(latitude.doubleValue(), longitude.doubleValue());
    }

    public double distanceKmTo(GeoPoint other) {
        return Haversine.distanceKm(latitude, longitude, other.latitude, other.longitude);
    }
}
