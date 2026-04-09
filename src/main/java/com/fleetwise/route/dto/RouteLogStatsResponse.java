package com.fleetwise.route.dto;

public record RouteLogStatsResponse(
        long totalTrips,
        double totalDistanceKm,
        Double averageEfficiencyScore) {
}
