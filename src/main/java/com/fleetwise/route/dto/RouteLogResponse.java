package com.fleetwise.route.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RouteLogResponse(
        UUID id,
        UUID vehicleId,
        UUID driverId,
        LocalDate tripDate,
        String originLabel,
        Double originLat,
        Double originLng,
        String destinationLabel,
        Double destinationLat,
        Double destinationLng,
        Double distanceKm,
        Integer estimatedDurationMin,
        Double actualFuelUsedLiters,
        Double expectedFuelLiters,
        Double efficiencyScore,
        String weatherCondition,
        Double temperatureCelsius,
        Instant createdAt) {
}
