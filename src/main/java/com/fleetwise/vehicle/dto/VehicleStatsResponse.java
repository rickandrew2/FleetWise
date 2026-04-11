package com.fleetwise.vehicle.dto;

import java.util.UUID;

public record VehicleStatsResponse(
        UUID vehicleId,
        long totalFuelLogs,
        double totalFuelLiters,
        double totalFuelCost,
        long totalTrips,
        double totalDistanceKm,
        Double averageEfficiencyScore) {
}