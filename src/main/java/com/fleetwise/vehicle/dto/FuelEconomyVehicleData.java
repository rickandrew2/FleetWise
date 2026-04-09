package com.fleetwise.vehicle.dto;

public record FuelEconomyVehicleData(
                Double combinedMpg,
                Double cityMpg,
                Double highwayMpg,
                String fuelType) {
}