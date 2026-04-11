package com.fleetwise.vehicle.dto;

public record EpaVehicleOption(
        int epaVehicleId,
        String label,
        Double combinedMpg,
        String fuelType) {
}