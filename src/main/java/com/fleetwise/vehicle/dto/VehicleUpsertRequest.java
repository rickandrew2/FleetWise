package com.fleetwise.vehicle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VehicleUpsertRequest(
                @NotBlank @Size(max = 20) String plateNumber,
                @NotBlank @Size(max = 50) String make,
                @NotBlank @Size(max = 50) String model,
                @Min(1980) @Max(2100) Integer year,
                @Size(max = 30) String fuelType,
                @Min(1) @Max(1000) Double tankCapacityLiters,
                Integer epaVehicleId,
                UUID assignedDriverId) {
}