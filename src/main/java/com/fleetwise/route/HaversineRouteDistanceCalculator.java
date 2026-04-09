package com.fleetwise.route;

import org.springframework.stereotype.Component;

@Component
public class HaversineRouteDistanceCalculator implements RouteDistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double ASSUMED_AVERAGE_SPEED_KMH = 40.0;

    @Override
    public DistanceResult calculate(double originLat, double originLng, double destinationLat, double destinationLng) {
        double distanceKm = haversine(originLat, originLng, destinationLat, destinationLng);
        int estimatedDurationMin = distanceKm <= 0
                ? 0
                : Math.max(1, (int) Math.ceil((distanceKm / ASSUMED_AVERAGE_SPEED_KMH) * 60.0));
        return new DistanceResult(distanceKm, estimatedDurationMin);
    }

    private double haversine(double originLat, double originLng, double destinationLat, double destinationLng) {
        double originLatRad = Math.toRadians(originLat);
        double destinationLatRad = Math.toRadians(destinationLat);
        double deltaLat = Math.toRadians(destinationLat - originLat);
        double deltaLng = Math.toRadians(destinationLng - originLng);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(originLatRad) * Math.cos(destinationLatRad)
                        * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
