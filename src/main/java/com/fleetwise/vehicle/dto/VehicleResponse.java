package com.fleetwise.vehicle.dto;

import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
                UUID id,
                String plateNumber,
                String make,
                String model,
                Integer year,
                String fuelType,
                Double tankCapacityLiters,
                Integer epaVehicleId,
                Double combinedMpg,
                Double cityMpg,
                Double highwayMpg,
                UUID assignedDriverId,
                Instant createdAt) {
}