package com.deliverysaas.shared.geo;

public class Haversine {

    private static final double radius_km = 6371.0;

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double sinDLat = Math.sin(dLat / 2);
        double sinDLon = Math.sin(dLon / 2);

        double a = sinDLat * sinDLat
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * sinDLon * sinDLon;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radius_km * c;
    }
}