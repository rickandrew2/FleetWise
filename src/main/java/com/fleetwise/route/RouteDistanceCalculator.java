package com.fleetwise.route;

public interface RouteDistanceCalculator {

    DistanceResult calculate(double originLat, double originLng, double destinationLat, double destinationLng);

    record DistanceResult(double distanceKm, int estimatedDurationMin) {
    }
}
