package com.fleetwise.fuellog.dto;

public record FuelLogStatsResponse(
        long totalLogs,
        Double totalCost,
        Double averageLitersPerLog) {
}
