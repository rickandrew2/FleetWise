package com.fleetwise.route.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record RouteLogUpsertRequest(
        @NotNull UUID vehicleId,
        UUID driverId,
        @NotNull LocalDate tripDate,
        @Size(max = 150) String originLabel,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double originLat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double originLng,
        @Size(max = 150) String destinationLabel,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double destinationLat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double destinationLng,
        @DecimalMin("0.0") Double actualFuelUsedLiters) {
}
