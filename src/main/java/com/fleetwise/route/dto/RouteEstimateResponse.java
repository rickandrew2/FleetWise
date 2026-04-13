package com.fleetwise.route.dto;

public record RouteEstimateResponse(
        Double distanceKm,
        Integer durationMin,
        Double expectedLiters,
        Double estimatedCost,
        Double currentPricePerLiter,
        String vehicleName,
        String fuelType) {
}
